package controllers

import akka.actor.ActorSystem
import dao.SpecializedUsers
import play.api.mvc.{Action, Controller}

import scala.concurrent.Await
import scala.concurrent.duration._

object Publishers extends Controller{
  implicit val system = ActorSystem()
  val users = new SpecializedUsers()

  def addTrustedPublisher(category:String,name:String)= Action {
    users.addUser(name,category)
    Ok("added")
  }

  def deletePublisher(name:String) = Action {
    users.remove(name)
    Ok("removed")
  }

  def showAll() = Action {
    val publishers = Await.result(users.getAll,5 seconds)
    publishers.foreach(x=>print(x))
    Ok(publishers.mkString(" "))
  }

  def check(name:String) = Action{
    val exist=Await.result(users.checkUser(name),2 seconds)
    Ok(exist.toString)
  }
}
