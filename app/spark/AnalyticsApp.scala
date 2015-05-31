package spark

/**
 * Created by bryan on 5/30/15.
 */

import db.QueueData
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import play.api.libs.json.Json

object AnalyticsApp {
  def main(args: Array[String]) {
    val logFile = "YOUR_SPARK_HOME/README.md" // Should be some file on your system
    val conf = new SparkConf().setAppName("Simple Application")
    val sc = new SparkContext(conf)
    val logData = sc.textFile("s3n://AKIAJ6GZ5D7G7KE7GI3A:iPR4xZjwzdbj0JZMmTxRoi0dFOpXE5e4vPuVfPz2@datalogs-streamchannel/*").cache()
    logData.foreach(x=>println(x))
    val decodedData = logData.map(x=>Json.parse(x).validate[Seq[QueueData]]).flatMap(x=>x.get)
    println(decodedData.count())
    decodedData.foreach(x=>println(x))

  }
}