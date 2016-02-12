package classification

import org.apache.spark.SparkContext
import org.apache.spark.mllib.feature.{IDF, HashingTF}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream
import twitter4j.Status

import scala.collection.mutable.HashMap

class Algorithms(sc: SparkContext, stream: DStream[Status], lemmatized:RDD[Seq[String]]) {

  val hashingTF = new HashingTF()

  val tf = hashingTF.transform(lemmatized)

  tf.cache()

  val idf = new IDF().fit(tf)

  val tfidf = idf.transform(tf)

  val docTermFreqs = lemmatized.map(terms => {
    val termFreqs = terms.foldLeft(new HashMap[String, Int]()) {
      (map, term) => {
        map += term -> (map.getOrElse(term, 0) + 1)
        map
      }
    }
    termFreqs
  })

  docTermFreqs.cache()

  val docFreqs = docTermFreqs.flatMap(_.keySet).map((_, 1)).
    reduceByKey(_ + _)

  val numTerms = 50000
  val ordering = Ordering.by[(String, Int), Int](_._2)
  val topDocFreqs = docFreqs.top(numTerms)(ordering)

  val numDocs = docTermFreqs.count()
  val idfs = docFreqs.map {
    case (term, count) => (term, math.log(numDocs.toDouble / count))
  }.collectAsMap()

  val idTerms = idfs.keys.zipWithIndex.toMap
  val termIds = idTerms.map(_.swap)

  val bIdfs = sc.broadcast(idfs).value
  val bIdTerms = sc.broadcast(idTerms).value

  val vecs = docTermFreqs.map(termFreqs => {
    val docTotalTerms = termFreqs.values.sum
    val termScores = termFreqs.filter {
      case (term, freq) => bIdTerms.contains(term)
    }.map {
      case (term, freq) => (bIdTerms(term),
        bIdfs(term) * termFreqs(term) / docTotalTerms)
    }.toSeq
    Vectors.sparse(bIdTerms.size, termScores)
  })

  vecs.foreach(x=>println("For exemple"+x.toString))
}
