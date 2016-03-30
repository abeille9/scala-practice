package classification

import akka.actor.ActorSystem
import dao.{CassandraDB, SpecializedUsers}
import org.apache.spark.streaming.twitter._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import play.api.Play
import twitter4j.{User, TwitterFactory}
import twitter4j.auth.AccessToken

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

object TwitterStream {

  val ssc = new StreamingContext(SparkConfig.sc, Seconds(10))

  val consumerKey = Play.current.configuration.getString("consumer_key").get
  val consumerSecret = Play.current.configuration.getString("consumer_secret").get
  val accessToken = Play.current.configuration.getString("access_token").get
  val accessTokenSecret = Play.current.configuration.getString("access_token_secret").get

  val twitter = new TwitterFactory().getInstance()
  twitter.setOAuthConsumer(consumerKey, consumerSecret)
  twitter.setOAuthAccessToken(new AccessToken(accessToken, accessTokenSecret))

  val stream = TwitterUtils.createStream(ssc, Option(twitter.getAuthorization)).filter(_.getLang == "en")

  def startTwitterStream() {
    val stopWords = SparkConfig.sc.broadcast(
      scala.io.Source.fromFile("public/data/stopwords.txt").getLines().toSet).value

    val cleanedDStream = TextCleaner.clear(stream, stopWords)

    implicit val system = ActorSystem()
    val users = new SpecializedUsers()
    val specializedUsers = Await.result(users.getAll, 5 seconds)

    def mapToCategories(iter: Iterator[(User, Seq[String])]) = {
      implicit val system = ActorSystem()
      val users = new SpecializedUsers()

      iter.map(x => (Await.result(users.getCategory(x._1.getScreenName), 1 seconds), x._2))
    }

    val specializedTweets = cleanedDStream.filter(user =>
      specializedUsers.contains(user._1.getScreenName))
      .mapPartitions(mapToCategories)

    val tweetsToCategorize = cleanedDStream.filter(user => !specializedUsers.contains(user._1.getScreenName))

    //specializedTweets.reduceByKeyAndWindow((a: Seq[String], b: Seq[String]) => a.toList ::: b.toList, Seconds(3600), Seconds(3500))

    specializedTweets
      .reduceByKeyAndWindow((a: Seq[String], b: Seq[String]) => a.toList ::: b.toList, Seconds(600), Seconds(600))
      .foreachRDD(rdd => {
        val count = rdd.count()
        if (count > 0) {
          val alg = new Algorithms(rdd)
          CassandraDB.insertConcepts(rdd.map(x => (x._1, x._2, alg.bIdfs)))
          print("Specialized tweets nr" + count)
          rdd.foreach(x => print(x._1 + ":" + x._2.mkString))
        }
      })
    specializedTweets.print()

    ssc.start()
    ssc.awaitTermination()
  }


  def stopStream() = {
    stream.context.stop(stopSparkContext = false, stopGracefully = true)
  }

}
