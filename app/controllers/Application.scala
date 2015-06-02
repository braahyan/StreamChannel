package controllers

import db._
import org.joda.time.DateTime
import play.Logger
import play.api.Play.current
import play.api.db.DB
import play.api.libs.json._
import play.api.mvc._


object Application extends Controller {
  val queueDataRepo = new QueueDataRepository
  val referrerDataRepo = new ReferrerDataRepository
  val visitDataRepo = new VisitDataRepository

  def addData() = Action(parse.json) { implicit request =>
    request.body.validate[QueueData]
      .fold(
      jsonWithErrors => {
        Logger.error(jsonWithErrors.toString)
        BadRequest(Json.obj(("message","failure")))
      },
      data => {
        DB.withTransaction {
          implicit conn =>
            queueDataRepo.addDataToQueue(data.withTimeStamp(DateTime.now()))
        }
        Ok(Json.obj(("message","success")))
      }
    )
  }

  def getData() = Action {implicit request =>
      DB.withTransaction(implicit conn =>
        Ok(Json.toJson(queueDataRepo.getDataFromQueue()))
    )
  }

  def getReferrerData() = Action {implicit request =>
    DB.withTransaction(implicit conn =>
      Ok(Json.toJson(referrerDataRepo.GetData()))
    )
  }

  def getVisitData() = Action{implicit request =>
    DB.withTransaction(implicit conn =>
      Ok(Json.toJson(visitDataRepo.GetData()))
    )
  }

}