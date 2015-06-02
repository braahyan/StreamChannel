package db

import java.sql.Connection

import org.joda.time.DateTime
import play.api.libs.json.{JsValue, Json}

/**
 * Created by bryan on 5/29/15.
 */
trait QueueDataRepositoryTrait {


  def addDataToQueue(data: QueueData)(implicit conn: Connection): Option[Long]

  def getDataFromQueue()(implicit conn: Connection): List[QueueData]

  def purgeDataFromQueue()(implicit conn: Connection): Int

  def getQueueLength()(implicit conn: Connection): Int
}

case class QueueData(data: JsValue, serverTime: Option[DateTime]) {
  def withTimeStamp(serverTimestamp: DateTime): QueueData = {
    serverTime match {
      case Some(_) => this
      case None => this.copy(serverTime = Some(serverTimestamp))
    }
  }
}

object QueueData {
  implicit val queueDataFormat = Json.format[QueueData]
}

case class WebEvent(document_location: String, referrer: Option[String], event: String, id: Int, first_visit: Boolean)

object WebEvent {
  implicit val webeventReads = Json.reads[WebEvent]
}

case class ReferrerData(date:DateTime, website:String, referrer:String, value:Int)

object ReferrerData{
  implicit val referrerDataFormat = Json.format[ReferrerData]
}

case class VisitData(date:DateTime, website:String, value:Int)

object VisitData{
  implicit val visitDataFormat = Json.format[VisitData]
}