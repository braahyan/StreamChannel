package spark

/**
 * Created by bryan on 5/30/15.
 */

import java.net.URL

import com.typesafe.config._
import db.{VisitData, ReferrerData, QueueData, WebEvent}
import org.apache.spark.{SparkConf, SparkContext}
import org.joda.time.DateTime
import play.api.libs.json.Json

object AnalyticsApp {

  def main(args: Array[String]) {
    val logFile = "YOUR_SPARK_HOME/README.md" // Should be some file on your system
    val conf = new SparkConf().setAppName("Simple Application")
    val appConfig = ConfigFactory.load()
    val awsCredentialFragment = s"${appConfig.getString("streamchannel.aws.clientKey")}:${appConfig.getString("streamchannel.aws.clientSecret")}"
    val bucketName = appConfig.getString("streamchannel.s3.bucketName")
    val inputPattern = appConfig.getString("streamchannel.s3.readTarget")
    val outputKey = appConfig.getString("streamchannel.s3.writeTarget")


    val sc = new SparkContext(conf)

    val logData = sc.textFile(s"s3n://$awsCredentialFragment@$bucketName/$inputPattern*") // forward slash here causes permissions error, why?
    val decodedData = logData.map(x => Json.parse(x).validate[Seq[QueueData]]).flatMap(x => x.get)
    val webEventData = decodedData.map(x => (x.serverTime.get, x.data.validate[WebEvent].get)).cache()

    def hourOfDayDateTime(date:DateTime) = {
      new DateTime(date.getYear, date.getMonthOfYear, date.getDayOfMonth, date.getHourOfDay,0)
    }


    // unique visitors on site per hour
    val webEventDataByHourAndSite = webEventData.groupBy(x => (hourOfDayDateTime(x._1), new URL(x._2.document_location).getHost)).map(x => (x._1, x._2.map(x => x._2)))
    val somedata = webEventDataByHourAndSite.map(x => Json.stringify(Json.toJson(VisitData(x._1._1, x._1._2, x._2.groupBy(x => x.id).keys.size))))
    somedata.saveAsTextFile(s"s3n://$awsCredentialFragment@$bucketName/$outputKey/webeventsByHour")

    // referrers per hour per site
    webEventDataByHourAndSite.map(x => (x._1,
      x._2.filter(x => x.first_visit).map(x => x.referrer match {
        case None => "direct"
        case Some(y) => y
      }).groupBy(x => x).map(x => (x._1, x._2.size)))).flatMap(x => x._2.map(y => Json.stringify(Json.toJson(ReferrerData(x._1._1, x._1._2, y._1, y._2)))))
      .saveAsTextFile(s"s3n://$awsCredentialFragment@$bucketName/$outputKey/referrersByHour")
  }

}