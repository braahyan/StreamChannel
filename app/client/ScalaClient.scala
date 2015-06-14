package client

import db.{WebEvent, DataEvent, DataEvent$}
import org.joda.time.DateTime
import play.Logger
import play.api.libs.json.Json

import scala.util.Random
import scalaj.http.Http

/**
 * Created by bryan on 5/29/15.
 */
object ScalaClient {
  def pushData(data: DataEvent) = {
    val jsonString = Json.stringify(Json.toJson(data))
    val resp = Http("http://localhost:9000/adddata").header("Content-Type", "application/json").postData(jsonString).asString
    Logger.info(resp.body)
    println(resp.body)
  }

  def randomElement[T](sequence:Seq[T]) = {
    sequence(Random.nextInt(sequence.length))
  }

  def genData() {

    // this was a script that i have included in the project to simplifiy things for the moment

    val websites = Seq("bareia.com","pvp.com","ole.com","bart.com")

    val referrers = Seq(Some("google.com"),Some("reddit.com"),Some("facebook.com"),Some("twitter.com"),
      Some("linkedin.com"),Some("thrivehive.com"),None)
    val pages = Seq("index.html", "aboutus.html", "contactus.html", "services.html", "pricing.html", "ourteam.html")
    val entryPages = Seq("index.html", "landingpage.html")
    val forms = Map(("contactus.html", "contact-form"), ("landingpage.html", "content-download"))


    def makeUrl(domain:String, page:String) = {
      s"http://$domain/$page"
    }

    val startTime = DateTime.now()
    for (x <- 1 to 10000) {
      var time = startTime.plusSeconds(Random.nextInt(10000))
      var website = randomElement(websites)
      var document_location = makeUrl(website, randomElement (entryPages))
      var referrer: Option[String] = randomElement(referrers) match{
        case None => None
        case Some(y)=> Some(makeUrl(y,""))
      }
      for (y <- 1 to Random.nextInt(10)) {
        val firstVisit = if (y ==1 ){true}else{false}
        val queueData = DataEvent(
        WebEvent(document_location,referrer,"page-view",x,firstVisit),
        Some(time)

          /*Json.obj(
            ("first_visit",firstVisit),
            ("event", "page-view"),
            ("id", x),
            ("document_location", document_location),
            ("referrer", referrer)),
          Some(time)*/)
        ScalaClient.pushData(queueData)
        println(queueData)
        referrer = Some(document_location)
        document_location = makeUrl(website, randomElement(pages))
        time = time.plusSeconds(Random.nextInt(2000)) // greater than number of seconds in 30 minutes
      }
      website = randomElement(websites)
    }
  }
}
