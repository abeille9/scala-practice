package training

import akka.actor.ActorSystem
import com.datastax.spark.connector._
import classification.TextCleaner
import com.datastax.spark.connector.SomeColumns
import config.{SparkConfig, TwitterConf}
import dao.SpecializedUsers

import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps

object SpecializedTweets extends SparkConfig{

  def receiveTweets() = {
    implicit val system = ActorSystem()
    val users = new SpecializedUsers()
    val specializedUsers = users.userCategory()

    var userCategory = collection.mutable.Map[String,String]()
    var set = Seq[(String, String,List[String])]()

    specializedUsers.keys.foreach(user =>
      try {
        val statuses = TwitterConf.getOrCreate().getUserTimeline(user)
        val it = statuses.iterator()
        var status = new ArrayBuffer[String]()
        while (it.hasNext) {
           status ++= TextCleaner.clear(it.next().getText)
        }
        set = set :+ (user,specializedUsers(user),status.toList)
      }
      catch {
        case e: Exception => println(user)
          println(e.getMessage)
      })

    if(set.nonEmpty){
      val result = sc.parallelize(set)
      result.saveToCassandra("semanticsna","tweets",SomeColumns("userid", "category", "words" append))

      val categoryUser = result.map(record=>(record._2,record._3))
      val groups = categoryUser.reduceByKey(_.++(_))

      groups.foreach(x=>print(x._1, x._2.length))
    }

  }

  def printMemUsage() = {
    val mb = 1024*1024
    val runtime = Runtime.getRuntime
    println("** Used Memory:  " + (runtime.totalMemory - runtime.freeMemory) / mb)
    println("** Free Memory:  " + runtime.freeMemory / mb)
    println("** Total Memory: " + runtime.totalMemory / mb)
    println("** Max Memory:   " + runtime.maxMemory / mb)
  }


}
