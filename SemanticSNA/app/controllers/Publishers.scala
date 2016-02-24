package controllers

import akka.actor.ActorSystem
import dao.SpecializedUsers
import play.api.libs.json.{JsError, Json}
import play.api.mvc.{BodyParsers, Action, Controller}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

object Publishers extends Controller {
  implicit val system = ActorSystem()
  val users = new SpecializedUsers()

  def addPublisher() = Action(BodyParsers.parse.json) { request =>

    request.body.validate[dto.Publisher].fold(
      errors => {
        BadRequest(Json.obj("status" -> "Invalid format", "error" -> JsError.toJson(errors)))
      },
      publisher => {
        users.addUser(publisher.name,publisher.category)
        Ok("Succes")
      }
    )
  }

  def deletePublisher(name: String) = Action {
    users.remove(name)
    Ok("removed")
  }

  def showAll() = Action {
    val publishers = Await.result(users.getAll, 5 seconds)
    var response = new ArrayBuffer[dto.Publisher]()
    publishers.foreach(x => response.+=(dto.Publisher(x, Await.result(users.getCategory(x), 2 seconds))))
    Ok(Json.toJson(dto.Publishers(response.sortBy(_.category).toList)))
  }

  def check(name: String) = Action {
    val exist = Await.result(users.checkUser(name), 2 seconds)
    Ok(exist.toString)
  }
}
