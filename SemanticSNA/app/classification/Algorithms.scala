package classification

import java.util.Locale

import akka.actor.ActorSystem
import config.SparkConfig
import dao.{SpecializedUsers, CassandraDB}
import models.Concept
import com.datastax.spark.connector._
import org.apache.spark.SparkContext
import org.apache.spark.mllib.classification.{LogisticRegressionWithLBFGS, NaiveBayes}
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.feature.{IDF, HashingTF}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.linalg.distributed.RowMatrix
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.stat.{MultivariateStatisticalSummary, Statistics}
import org.apache.spark.mllib.tree.{RandomForest, DecisionTree}
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream
import twitter4j.Status

import scala.collection.mutable.{ArrayBuffer, HashMap}
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

object Algorithms extends SparkConfig {

  val rdd = sc.cassandraTable("semanticsna","tweets")
  val lemmatized: RDD[(String, List[String])] = rdd
    .map(row => (row.getString(0), row.get[List[String]]("words")))
    .reduceByKey(_.++(_))

  val docTermFreqsWithCategory = lemmatized.map(terms => {
    val termFreqs = terms._2.foldLeft(new HashMap[String, Int]()) {
      (map, term) => {
        map += term -> (map.getOrElse(term, 0) + 1)
        map
      }
    }
    //CassandraDB.write(terms._1,termFreqs)
    (terms._1,termFreqs)
  })

  val docTermFreqs : RDD[HashMap[String, Int]] = docTermFreqsWithCategory.map(_._2)

  docTermFreqs.cache()

  val docFreqs = docTermFreqs.flatMap(_.keySet).map((_, 1)).
    reduceByKey(_ + _)

  val numTerms = 30000
  val ordering = Ordering.by[(String, Int), Int](_._2)
  val topDocFreqs = docFreqs.top(numTerms)(ordering)

  def getWordsForCategory(category:String) : List[(String, Int)]= {
    docTermFreqsWithCategory.filter(_._1 == category).first()._2.toList
  }

  val numDocs = docTermFreqs.count()
  println("Doc frecv "+topDocFreqs.length)
  val idcat = docTermFreqsWithCategory.map{
    case (category,words) => category
  }.collect().zipWithIndex
  val catIds = idcat.map(_.swap)


  val idfs = docFreqs.map{
    case (term, count) => (term, count)
  }.collectAsMap()

  val idTerms = idfs.keys.zipWithIndex.toMap
  val termIds = idTerms.map(_.swap)

  val bIdfs = sc.broadcast(idfs).value
  val bIdTerms = sc.broadcast(idTerms).value

  var i:Int = 0
  val vecs = docTermFreqs.map(termFreqs => {
    val docTotalTerms = termFreqs.values.sum
    val termScores = termFreqs.filter {
      case (term, freq) => bIdTerms.contains(term)
    }.map {
      case (term, freq) =>
        (bIdTerms(term),
        bIdfs(term) * termFreqs(term).toDouble / docTotalTerms)
        //Concept(term,bIdfs(term) * termFreqs(term) / docTotalTerms,i.toString)
    }.toSeq
    //termScores//.sortBy(_._2).reverse.take(15).foreach(term => println(termIds(term._1)+term._2))
    Vectors.sparse(bIdTerms.size, termScores)
  })

  vecs.cache()
  val mat = new RowMatrix(vecs)
  val k = 4

  def show() = {

    val summary: MultivariateStatisticalSummary = Statistics.colStats(vecs)
    val numNonzeros =  if( summary.numNonzeros.numNonzeros > k){
      k
    } else {
      summary.numNonzeros.numNonzeros
    }
    //vecs.collect().foreach(vector => vector)
    //    val svd = mat.computeSVD(numNonzeros, computeU=true)
    //    val v = svd.V

    //iter.map(x=>(term:String, count:Int ) => (term, math.log(numDocs.toDouble / count)))
    //vecs.foreach(x=>println("For exemple "+x.size))

    val category = lemmatized.map(el => {
      implicit val system = ActorSystem()
      val users = new SpecializedUsers()
      Await.result(users.getCategory(el._1),5 seconds)
    })
    val map = category.collect()
    //val tesCattId = map.map(_.swap)
    var i:Int = -1
    val trainingData = vecs.map(v => {
      i=i+1
      val label = {
        map(i) match {
          case "economy" => 0
          case "education" => 1
          case "sport" => 2
          case "psychology" => 3
          case _ => 4
        }
      }
      LabeledPoint(label,v)})
    //model.save(sc, "target/tmp/myNaiveBayesModel")
    val splits = trainingData.randomSplit(Array(0.7, 0.3), seed = 11L)

    val training = splits(0)
    training.cache()

    val test = splits(1)

    println("Test lemmas " + test.count())
    val model = NaiveBayes.train(training, lambda = 1.0, modelType = "multinomial")

    model.labels.foreach(cat => println(cat + " : " + catIds(cat.toInt)))

    val predictionAndLabel = test.map(p => (p.label, model.predict(p.features)))

    predictionAndLabel.take(10).foreach(x => println(x._1 + " Naive Bayes: " + x._2))
    val accuracy = predictionAndLabel.filter(x => x._1 == x._2).count().toDouble / test.count()

    println("Accuracy Naive Bayes: " + accuracy)

    val model2 = new LogisticRegressionWithLBFGS()
      .setNumClasses(4)
      .run(training)

    // Compute raw scores on the test set.
    val predictionAndLabels = test.map { case LabeledPoint(label, features) =>
      val prediction = model2.predict(features)
      (label, prediction)
    }
    predictionAndLabels.take(10).foreach(x => println(x._1 + " LogisticRegression " + x._2))

    // Get evaluation metrics.
    val metrics = new MulticlassMetrics(predictionAndLabels)
    val precision = metrics.precision
    println("Precision Logistic Regresion = " + precision)

    val categoricalFeaturesInfo = Map[Int, Int]()
    val maxDepth = 4
    val maxBins = 80


    val numClasses = 4
    val numTrees = 5 // Use more in practice.
    val featureSubsetStrategy = "auto" // Let the algorithm choose.

    val model4 = RandomForest.trainClassifier(training, numClasses, categoricalFeaturesInfo,
      numTrees, featureSubsetStrategy, "gini", maxDepth, maxBins)

    // Evaluate model on test instances and compute test error
    val labelAndPreds = test.map { point =>
      val prediction = model4.predict(point.features)
      (point.label, prediction)
    }

    labelAndPreds.take(10).foreach(x => println(x._1 + "  Random Forest " + x._2))
    val testErr = labelAndPreds.filter(r => r._1 == r._2).count.toDouble / test.count()
    println("Random Forest Accuracy = " + testErr)

  }

  def gettopTerms = {
    val topTerms = new ArrayBuffer[Seq[(String, Double)]]()
    val summary: MultivariateStatisticalSummary = Statistics.colStats(vecs)
    val numNonzeros =  if( summary.numNonzeros.numNonzeros > k){
      k
    } else {
      summary.numNonzeros.numNonzeros
    }
    //vecs.first().toSparse.
    val svd = mat.computeSVD(numNonzeros, computeU=true)
    val v = svd.V
    val arr = v.toArray
    for (i <- 0 until 5800) {
      val offs = i * v.numRows
      val termWeights = arr.slice(offs, offs + v.numRows).zipWithIndex
      val sorted = termWeights.sortBy(-_._1)
      topTerms += sorted.take(numTerms).map{
        case (score, id) => (termIds(id), score)
      }
    }
    topTerms
  }


  def getTermById(id:Integer):String = {
    idTerms.find(p => p._2 == id) match {
      case Some(term) => term._1
      case _ => ""
    }
  }

}
