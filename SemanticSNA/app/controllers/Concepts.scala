package controllers

import dao.CassandraDB
import play.api.mvc.{Action, Controller}

object Concepts extends Controller {

  def Show() = Action {
    CassandraDB.printTestData()
    Ok("All data from db")
  }

}
