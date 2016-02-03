package classification

import org.apache.spark.streaming.{ Seconds, StreamingContext }
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.streaming.twitter._
import play.api.Play
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken

object TwitterStream {
  val driverPort = 8080
  val driverHost = "localhost"
  val conf = new SparkConf(false) // skip loading external settings
    .setMaster("local[*]") // run locally with enough threads
    .setAppName("firstSparkApp")
    .set("spark.driver.port", s"$driverPort")
    .set("spark.driver.host", s"$driverHost")
    .set("spark.akka.logLifecycleEvents", "true")

  val sc=new SparkContext(conf)
  val ssc = new StreamingContext(sc, Seconds(10))

  val stopWords = sc.broadcast(
    scala.io.Source.fromFile("public/data/stopwords.txt").getLines().toSet).value

  def startTwitterStream() {
    val consumerKey = Play.current.configuration.getString("consumer_key").get
    val consumerSecret = Play.current.configuration.getString("consumer_secret").get
    val accessToken = Play.current.configuration.getString("access_token").get
    val accessTokenSecret = Play.current.configuration.getString("access_token_secret").get

    val twitter = new TwitterFactory().getInstance()
    twitter.setOAuthConsumer(consumerKey, consumerSecret)
    twitter.setOAuthAccessToken(new AccessToken(accessToken, accessTokenSecret))

    val stream = TwitterUtils.createStream(ssc, Option(twitter.getAuthorization)).filter(_.getLang == "en")

    val preparedData = TextCleaner.clear(stream,stopWords)

    preparedData.print(25)

    ssc.start()
    ssc.awaitTermination()

  }

  def stopStream() = {
    ssc.stop(true, true)
  }

}
