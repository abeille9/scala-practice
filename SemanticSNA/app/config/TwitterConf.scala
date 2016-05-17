package config

import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.twitter.TwitterUtils
import play.api.Play
import twitter4j.{Twitter, TwitterFactory}
import twitter4j.auth.{Authorization, AccessToken}

object TwitterConf {

  val consumerKey = Play.current.configuration.getString("consumer_key").get
  val consumerSecret = Play.current.configuration.getString("consumer_secret").get
  val accessToken = Play.current.configuration.getString("access_token").get
  val accessTokenSecret = Play.current.configuration.getString("access_token_secret").get

  private val twitter = new TwitterFactory().getInstance()
  twitter.setOAuthConsumer(consumerKey, consumerSecret)
  twitter.setOAuthAccessToken(new AccessToken(accessToken, accessTokenSecret))

  def getOrCreate(): Twitter = {
      if (twitter == null) {
        new TwitterFactory().getInstance()
      }
      twitter
  }

  def stream (ssc:StreamingContext)= TwitterUtils
    .createStream(ssc, Option(twitter.getAuthorization))
    .filter(x=>x.getLang == "en")
}
