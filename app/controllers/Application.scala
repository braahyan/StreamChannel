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

}