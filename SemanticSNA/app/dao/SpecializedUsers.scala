package dao

import akka.actor.ActorSystem
import redis.RedisClient

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

class SpecializedUsers extends Serializable{
  implicit val system = ActorSystem()
  def SpecializedUsers()={}
  def redis = new RedisClient("localhost", 6379)

  def addUser(name: String, category: String) = {
    redis.set(name, category)
    redis.bgsave()
  }

  def checkUser(name: String) = {
    redis.exists(name)
  }

  def getCategory (name: String) ={
    redis.get(name).map {
      case Some(category) => category.decodeString("UTF8")
      case None => "unknown"
    }
  }

  def getAll = {
    redis.keys("*")
  }

  def userCategory() : Map[String,String]= {
    var map = for {key <- Await.result(getAll,2 seconds)} yield {
      Tuple2(key,Await.result(getCategory(key),2 second))
    }
    map.toMap
  }

  def remove(name: String) = {
    redis.del(name)
  }
}
