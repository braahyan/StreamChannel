package controllers

import DB.QueueDataRepository
import org.joda.time.DateTime
import play.api.db.DB
import play.api.mvc._
import play.api.libs.json._
import play.api.Play.current

object Application extends Controller {

  val queueDataRepo = new QueueDataRepository

  def index = Action{
    Ok("'sup")
  }


  def addData = Action { request =>
    //DB.withTransaction {
    //  implicit conn =>
    //    queueDataRepo.addDataToQueue(DateTime.now(), "")
    //}
    Ok(Json.obj(("hrmagerd", "wooooooooh")))
  }

  def getData = Action { request =>
    //DB.withTransaction {
    //  implicit conn =>
    //    queueDataRepo.addDataToQueue(DateTime.now(), "")
    //}
    Ok(Json.obj(("hrmagerd", "wooooooooh")))
  }

}