package classification

import akka.actor.ActorSystem
import config.{SparkConfig, TwitterConf}
import dao.SpecializedUsers
import org.apache.spark.mllib.classification.NaiveBayesModel
import org.apache.spark.mllib.feature.{IDF, HashingTF}
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}
import twitter4j.{Status, User}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

object TwitterStream extends SparkConfig{

  val ssc = StreamingContext.getActiveOrCreate(createStreamingContext)

  private def createStreamingContext(): StreamingContext = {
    new StreamingContext(sc, Seconds(60))
  }

  def startTwitterStream() {

    implicit val system = ActorSystem()
    val users = new SpecializedUsers()
    val specializedUsers = Await.result(users.getAll, 2 seconds)
    val stream: DStream[Status] = TwitterConf.stream(ssc)
    val cleanedDStream = TextCleaner.clear(stream)

    stream.foreachRDD(rdd => print("  Received " + rdd.count()))

    cleanedDStream.print()

    val tweetsToCategorize = cleanedDStream.filter(user => !specializedUsers.contains(user._1.getScreenName))

    val model = NaiveBayesModel.load(sc, "target/tmp/myNaiveBayesModel")
    val hashingTF = new HashingTF()

    tweetsToCategorize.foreachRDD(rdd =>{
      val tf = hashingTF.transform(rdd.map(x=>x._2))

      tf.cache()

      val idf = new IDF().fit(tf)

      val tfidf = idf.transform(tf)

      val predict = model.predict(tfidf)

    }
    )
    ssc.start()
    ssc.awaitTermination()
  }


  def stopStream() = {
    ssc.stop(stopSparkContext = false, stopGracefully = true)
  }

}
