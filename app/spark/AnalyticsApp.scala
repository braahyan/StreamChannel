package spark

/**
 * Created by bryan on 5/30/15.
 */

import java.net.URL

import com.typesafe.config._
import db.{QueueData, WebEvent}
import org.apache.spark.{SparkConf, SparkContext}
import org.joda.time.DateTime
import play.api.libs.json.Json

object AnalyticsApp {

  def main(args: Array[String]) {
    val logFile = "YOUR_SPARK_HOME/README.md" // Should be some file on your system
    val conf = new SparkConf().setAppName("Simple Application")
    val appConfig = ConfigFactory.load()
    val sc = new SparkContext(conf)
    val logData = sc.textFile(s"s3n://${appConfig.getString("streamchannel.aws.clientKey")}:${appConfig.getString("streamchannel.aws.clientSecret")}@datalogs-streamchannel/*")
    val decodedData = logData.map(x => Json.parse(x).validate[Seq[QueueData]]).flatMap(x => x.get)
    val webEventData = decodedData.map(x => (x.serverTime.get, x.data.validate[WebEvent].get)).cache()

    def getHourTuple(date:DateTime) ={
      (date.getYear, date.getMonthOfYear, date.getDayOfMonth, date.getHourOfDay)
    }

    // total events
    println(webEventData.count())

    // total unique visitors
    println(webEventData.groupBy(x=>x._2.id).count())

    // unique visitors on site per hour
    val webEventDataByHourAndSite = webEventData.groupBy(x=>(getHourTuple(x._1), new URL(x._2.document_location).getHost)).map(x=>(x._1, x._2.map(x=>x._2)))
    webEventDataByHourAndSite.map(x=>(x._1, x._2.groupBy(x=>x.id).keys.size)).foreach(x=>println(x))

    // referrers per hour per site
    val thisMonster = webEventDataByHourAndSite.map(x=>(x._1,
      x._2.filter(x=>x.first_visit).map(x=>x.referrer match{
      case None => "direct"
      case Some(y) => y
    }).groupBy(x=>x).map(x=>(x._1,x._2.size)))).foreach(x=>println(x))

  }

}