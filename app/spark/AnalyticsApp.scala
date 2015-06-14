package spark

/**
 * Created by bryan on 5/30/15.
 */
import java.net.URL

import com.typesafe.config._
import db._
import org.apache.spark.rdd.RDD
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
    val bucketUrl = s"s3n://$awsCredentialFragment@$bucketName"
    val inputUrl = s"$bucketUrl/$inputPattern"
    val outputUrl = s"$bucketUrl/$outputKey"
    val sc = new SparkContext(conf)



    val logData = sc.textFile(s"$inputUrl*") // forward slash here causes permissions error, why?
    // this is bad, in a production environment, we should never assume that we're getting the correct data shape
    // eventually it always fails
    val decodedData = logData.map(x => Json.parse(x).validate[Seq[DataEvent]]).flatMap(x => x.get)
    val webEventData = decodedData.map(x => (x.serverTime.get, x.data.validate[WebEvent].get)).cache()

    // write list of websites we have seen
    webEventData.map(x=>new URL(x._2.document_location).getHost + "\n").distinct()
      .saveAsTextFile(s"$outputUrl/websites")

    val groupedByHourAndSite = groupByHourAndSite(webEventData)
    groupedByHourAndSite.cache()

    eventsByHourAndSite(groupedByHourAndSite)
      .map(x=>Json.stringify(Json.toJson(x)))
      .saveAsTextFile(s"$outputUrl/webeventsByHour")

    referrersByHourAndSite(groupedByHourAndSite)
      .map(x=>Json.stringify(Json.toJson(x)))
      .saveAsTextFile(s"$outputUrl/referrersByHour")

    pageViewsByHourAndSite(groupedByHourAndSite)
      .map(x=>Json.stringify(Json.toJson(x)))
      .saveAsTextFile(s"$outputUrl/pagesByHour")

  }

  def hourOfDayDateTime(date:DateTime) = {
    new DateTime(date.getYear, date.getMonthOfYear, date.getDayOfMonth, date.getHourOfDay,0)
  }

  def groupByHourAndSite(rdd:RDD[(DateTime,WebEvent)]): RDD[((DateTime, String), Iterable[ WebEvent])] = {
    rdd
      .groupBy(x=>(hourOfDayDateTime(x._1), new URL(x._2.document_location).getHost))
      .map(x=>(x._1, x._2.map(x=>x._2)))
  }

  def eventsByHourAndSite(rdd: RDD[((DateTime, String), Iterable[WebEvent])]) = {
    rdd.map(x=>VisitData(x._1._1, x._1._2, x._2.size))
  }

  def pageViewsByHourAndSite(rdd: RDD[((DateTime, String), Iterable[WebEvent])]) = {
    rdd.map(x=>(x._1, x._2
      .groupBy(x=>new URL(x.document_location).getPath)
      .map(x=>(x._1,x._2.size)))).flatMap(x=>x._2.map(y=>PageData(x._1._1,x._1._2,y._1, y._2)))
  }

  def referrersByHourAndSite(rdd: RDD[((DateTime, String), Iterable[WebEvent])]) = {
    rdd.map(x => (x._1,
      x._2.filter(x=>x.hasOffsiteReferrer).map(x => x.referrer match {
        case None => "direct"
        case Some(y) => y
      }).groupBy(x => x).map(x => (x._1, x._2.size))))
      .flatMap(x => x._2.map(y => ReferrerData(x._1._1, x._1._2, y._1, y._2)))
  }

}