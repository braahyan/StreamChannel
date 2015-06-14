package db

import java.net.URL
import java.sql.Connection

import org.joda.time.{DateTime, Seconds}
import play.api.libs.json.Json

import scala.collection.mutable

/**
 * Created by bryan on 5/29/15.
 */
trait QueueDataRepositoryTrait {


  def addDataToQueue(data: DataEvent)(implicit conn: Connection): Option[Long]

  def getDataFromQueue()(implicit conn: Connection): List[DataEvent]

  def purgeDataFromQueue()(implicit conn: Connection): Int

  def getQueueLength()(implicit conn: Connection): Int
}

object Joda {
  implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)
}

case class DataEvent(data: WebEvent, serverTime: Option[DateTime]) {
  def withTimeStamp(serverTimestamp: DateTime): DataEvent = {
    serverTime match {
      case Some(_) => this
      case None => this.copy(serverTime = Some(serverTimestamp))
    }
  }
}

object DataEvent {
  implicit val wevent = Json.format[WebEvent]
  implicit val queueDataFormat = Json.format[DataEvent]
}

case class WebEvent(document_location: String, referrer: Option[String], event: String, id: Int, first_visit: Boolean){
  def hasOffsiteReferrer = {
    referrer match{
      case None => true
      case Some(y) => new URL(document_location).getHost != new URL(y).getHost
    }
  }
}

object WebEvent {
  implicit val webeventReads = Json.format[WebEvent]
}

case class ReferrerData(date:DateTime, website:String, referrer:String, value:Int)

object ReferrerData{
  implicit val referrerDataFormat = Json.format[ReferrerData]
}

case class VisitData(date:DateTime, website:String, value:Int)

object VisitData{
  implicit val visitDataFormat = Json.format[VisitData]
}

case class PageData(date:DateTime, website:String, page:String,value:Int)

object PageData{
  implicit val pageDataFormat = Json.format[PageData]
}

case class Website(website:String)

object Website{
  implicit val websiteFormat = Json.format[Website]
}

object LandingPageSegment{
  implicit val landingPageFormat = Json.format[LandingPageSegment]
}

case class LandingPageSegment(displayName:String, regex:String, forReferrer:Boolean, website:Option[String]){
  def matches(webEvent: WebEvent) = {
    webEvent.hasOffsiteReferrer &&
      ((forReferrer && webEvent.referrer.fold(false)(x=>x.matches(regex))) ||
        (!forReferrer && webEvent.document_location.matches(regex))
        ) &&
    website.fold(true)(x=>webEvent.document_location.matches(x))
  }
}

case class Visitor(webEvents:Seq[DataEvent]){
  // TODO:our session length in seconds, may want to make this configurable long term, but right now,
  // choosing the easy way out.
  val sessionLength = 30 * 60

  def getSessions:Seq[VisitorSession] = {
    import Joda._
    val sortedEvents = webEvents.sortBy(x=>x.serverTime.get)
    var previousEvent:DataEvent = sortedEvents.head
    var currentBuffer:mutable.Buffer[DataEvent] = mutable.Buffer(previousEvent)
    var myReturnBuffer = mutable.Buffer[mutable.Buffer[DataEvent]](currentBuffer)

    for(event <- sortedEvents.tail){
      if(Seconds.secondsBetween(previousEvent.serverTime.get, event.serverTime.get).getSeconds > sessionLength){

        currentBuffer = mutable.Buffer(event)
        myReturnBuffer += currentBuffer

      }
      else{
        currentBuffer += event
      }

      previousEvent = event
    }

    myReturnBuffer.map(x=>VisitorSession(x))
  }
}

case class VisitorSession(webEvents:Seq[DataEvent])