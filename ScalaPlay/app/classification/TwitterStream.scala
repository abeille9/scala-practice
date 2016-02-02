package classification

import java.util.Properties

import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{ Seconds, StreamingContext }
import org.apache.spark.ml.feature.{HashingTF, Tokenizer}
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.streaming.twitter._
import edu.stanford.nlp.pipeline._
import edu.stanford.nlp.ling.CoreAnnotations._
import play.api.Play
import twitter4j.{Status, TwitterFactory}
import twitter4j.auth.AccessToken
import scala.collection.JavaConversions._
import scala.collection.immutable.HashMap
import scala.collection.mutable.ArrayBuffer

object TwitterStream {
  val driverPort = 8080
  val driverHost = "localhost"
  val conf = new SparkConf(false) // skip loading external settings
    .setMaster("local[*]") // run locally with enough threads
    .setAppName("firstSparkApp")
    .set("spark.logConf", "true")
    .set("spark.driver.port", s"$driverPort")
    .set("spark.driver.host", s"$driverHost")
    .set("spark.akka.logLifecycleEvents", "true")

  val sc=new SparkContext(conf)
  val ssc = new StreamingContext(sc, Seconds(10))

  def createNLPPipeline(): StanfordCoreNLP = {
    val props:Properties = new Properties()
    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner")
    new StanfordCoreNLP(props)
  }

  def isOnlyLetters(str: String): Boolean = {
    str.forall(c => Character.isLetter(c))
  }

  def plainTextToLemmas(text: String,
                        pipeline: StanfordCoreNLP): Seq[String] = {
    val doc = new Annotation(text)
    pipeline.annotate(doc)
    val lemmas = new ArrayBuffer[String]()
    val sentences = doc.get(classOf[SentencesAnnotation])
    for (sentence <- sentences;
         token <- sentence.get(classOf[TokensAnnotation])) {
      val lemma = token.get(classOf[LemmaAnnotation])
      if (lemma.length > 2 && !stopWords.contains(lemma)
        && isOnlyLetters(lemma)) {
        lemmas += lemma.toLowerCase
      }
    }
    lemmas
  }

  val stopWords = sc.broadcast(
    scala.io.Source.fromFile("public/data/stopwords.txt").getLines().toSet).value

  def clear(stream: DStream[Status]) = {
   stream.flatMap(status=>plainTextToLemmas(status.getText,createNLPPipeline()))
  }

  def startTwitterStream() {
    // Twitter Authentication credentials
    val consumerKey = Play.current.configuration.getString("consumer_key").get
    val consumerSecret = Play.current.configuration.getString("consumer_secret").get
    val accessToken = Play.current.configuration.getString("access_token").get
    val accessTokenSecret = Play.current.configuration.getString("access_token_secret").get

    // Authorising with Twitter Application credentials
    val twitter = new TwitterFactory().getInstance()
    twitter.setOAuthConsumer(consumerKey, consumerSecret)
    twitter.setOAuthAccessToken(new AccessToken(accessToken, accessTokenSecret))

    //Select only English tweets
    val stream = TwitterUtils.createStream(ssc, Option(twitter.getAuthorization)).filter(_.getLang == "en")

    val preparedData = clear(stream)

    preparedData.print(25)

    ssc.start()
    ssc.awaitTermination()

  }

  def stopStream() = {
    ssc.stop()
  }

}
