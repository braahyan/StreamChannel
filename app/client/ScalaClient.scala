package client

import db.QueueData
import org.joda.time.DateTime
import play.Logger
import play.api.libs.json.Json

import scala.util.Random
import scalaj.http.Http

/**
 * Created by bryan on 5/29/15.
 */
object ScalaClient {
  def pushData(data: QueueData) = {
    val jsonString = Json.stringify(Json.toJson(data))
    val resp = Http("http://localhost:9000/adddata").header("Content-Type", "application/json").postData(jsonString).asString
    Logger.info(resp.body)
    println(resp.body)
  }

  def randomElement[T](sequence:Seq[T]) = {
    sequence(Random.nextInt(sequence.length))
  }

  def genData() {
    val referrers = Seq(Some("google.com"),Some("reddit.com"),Some("facebook.com"),Some("twitter.com"),
                    Some("linkedin.com"),Some("thrivehive.com"),None)
    val pages = Seq("index.html", "aboutus.html", "contactus.html", "services.html", "pricing.html", "ourteam.html")
    val entryPages = Seq("index.html", "landingpage.html")
    val forms = Map(("contactus.html", "contact-form"), ("landingpage.html", "content-download"))


    val startTime = DateTime.now()
    for (x <- 1 to 1000) {
      var time = startTime.plusSeconds(Random.nextInt(10000))
      var currentPage = randomElement (entryPages)
      var previousPage: Option[String] = randomElement(referrers)
      for (y <- 1 to Random.nextInt(10)) {
        ScalaClient.pushData(QueueData(Json.obj(("type", "web"), ("event", "web-visit"), ("id", x), ("current", currentPage), ("previous", previousPage)), time, Some(time)))
        previousPage = Some(currentPage)
        currentPage = randomElement(pages)
        time = time.plusSeconds(Random.nextInt(300))
      }
    }
  }
}
