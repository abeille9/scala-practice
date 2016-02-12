package dao

import akka.actor.ActorSystem
import redis.RedisClient

class SpecializedUsers(implicit system: ActorSystem) {
  val redis = new RedisClient("localhost", 6379)

  def addUser (name:String, category:String)= {
    redis.set(name,category)
    redis.bgsave()
  }

  def checkUser (name:String) = {
    redis.exists(name)
  }

  def getCategory (name:String) = {
    redis.get(name)
  }

  def getAll = {
    redis.keys("*")
  }

  def remove(name:String) = {
    redis.del(name)
  }
}
