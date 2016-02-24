package controllers

import play.api.mvc._
import classification.TwitterStream

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
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