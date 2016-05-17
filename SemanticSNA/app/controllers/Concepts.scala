package controllers

import akka.actor.ActorSystem
import classification.Algorithms
import classification.Algorithms._
import com.datastax.spark.connector._
import config.TwitterConf
import dao.{SpecializedUsers, CassandraDB}
import org.apache.spark.rdd.RDD
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import training.SpecializedTweets

import scala.collection.mutable.HashMap

object Concepts extends Controller {

  def Show() = Action {
    //Algorithms.show()
    //val topTerms = Algorithms.gettopTerms(2)
//    val concepts = topTerms.collect()
//    concepts.foreach(vector =>vector.)
    Ok("Done")
  }

  def StartTraining() = Action {
    Algorithms.show()
    Ok("Classified")
  }

  def getWords(category:String) = Action {
    val list = getWordsForCategory(category).sortBy(_._2).reverse.take(100)

     Ok(Json.toJson(list.map(x=>dto.Word(x._1,x._2))))
  }

  private  def getWordsForCategory(category:String) : List[(String, Int)]= {
    val rdd = sc.cassandraTable("semanticsna","tweets")
    val lemmatized: RDD[(String, List[String])] = rdd.map(row =>
      (row.getString(1), row.get[List[String]]("words")))
      .reduceByKey(_.++(_))

    //  val words = new ArrayBuffer[(String,scala.List[(String,Int)])]

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
    docTermFreqsWithCategory.filter(_._1 == category).first()._2.toList
  }

  def getCategories = Action {
    implicit val system = ActorSystem()
    val users = new SpecializedUsers()
    val categories = users.userCategory().values.toSet

    Ok(Json.toJson(categories))
  }

  def takeNewTweets = Action {
    SpecializedTweets.receiveTweets()
    Ok("New specialized tweets received")
  }
}

