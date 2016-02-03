package controllers

import play.api.mvc._
import classification.TwitterStream

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def startStream = Action{
    TwitterStream.startTwitterStream()
    Ok("Started")
  }

  def stopStream = Action {
    TwitterStream.stopStream()
    Ok("Stoped")
  }

}