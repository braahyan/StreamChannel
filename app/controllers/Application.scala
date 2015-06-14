package controllers

import db._
import org.joda.time.DateTime
import play.api.Play.current
import play.api.db.DB
import play.api.libs.json._
import play.api.mvc._


object Application extends Controller {
  val queueDataRepo = new DataEventRepository
  val referrerDataRepo = new ReferrerDataRepository
  val visitDataRepo = new VisitDataRepository
  val pageDataRepo = new PageDataRepository
  val websiteRepo = new WebsiteRepository

  def index() = Action{ implicit request =>
    Ok(views.html.index.render())
  }

  def addData() = Action(parse.json) { implicit request =>
    request.body.validate[DataEvent]
      .fold(
      jsonWithErrors => {
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

  def getReferrerData(website:String) = Action {implicit request =>
    DB.withTransaction(implicit conn => {
      val referrerData = referrerDataRepo.GetData(website)
      Ok(Json.toJson(referrerData.groupBy(x=>x.referrer)))
      }
    )
  }

  def getVisitData(website:String) = Action{implicit request =>
    DB.withTransaction(implicit conn => {
        val data = visitDataRepo.GetData(website)
        Ok(Json.toJson(data))
      }
    )
  }

  def getPageData(website:String) = Action { implicit request =>
    DB.withTransaction(implicit conn => {
        val data = pageDataRepo.GetData(website)
        Ok(Json.toJson(data.groupBy(x=>x.page)))
      }
    )
  }

  def getWebsites() = Action { implicit request =>
    DB.withTransaction(implicit conn => {
      val data = websiteRepo.GetData()
      Ok(Json.toJson(data))
    }
    )
  }

}