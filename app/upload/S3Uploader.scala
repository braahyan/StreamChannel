package upload

import awscala._
import awscala.s3._
import com.amazonaws.services.s3.model.ObjectMetadata
import db.QueueData
import play.api.Play
import play.api.libs.json._

import scala.io.Source


/**
 * Created by bryan on 5/29/15.
 */
class S3Uploader {
  implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)
  val bucketName = Play.current.configuration.getString("streamchannel.s3.bucketName").get
  val keyRoot = Play.current.configuration.getString("streamchannel.s3.readTarget").get
  implicit val s3 = S3(Play.current.configuration.getString("streamchannel.aws.clientKey").get,Play.current.configuration.getString("streamchannel.aws.clientSecret").get)(Region.US_EAST_1)

  def GenerateKeyData(beginning:String,queueEntries:Seq[QueueData]):(String, DateTime, DateTime) = {
    val dates = queueEntries.map(x=>x.serverTime.get)
    val maxDate = dates.max
    val minDate = dates.min
    (beginning + "/" + minDate.getMillis.toString + "-" + maxDate.getMillis.toString, minDate, maxDate)
  }

  def GetBucket(bucketName:String) : Bucket = {
    s3.bucket(bucketName) match{
      case None => throw new Exception()
      case Some(y) => y
    }
  }

  def UploadQueueEntries(queueEntries:Seq[QueueData]) :String = {

    val key = GenerateKeyData(keyRoot,queueEntries)


    val metadata = new ObjectMetadata()
    metadata.addUserMetadata("minDate", key._2.toString())
    metadata.addUserMetadata("maxDate", key._3.toString())
    metadata.addUserMetadata("count", queueEntries.length.toString)

    val jsValue = Json.toJson(queueEntries)
    val jsValueString = jsValue.toString.getBytes

    GetBucket(bucketName).putObject(key._1, jsValueString, metadata)
    key._1
  }

  def GetQueueEntries(key:String) : Seq[QueueData] = {
    val queueDataS3Object = GetBucket(bucketName).get(key)
      match {
      case None => throw new Exception()
      case Some(y) => y
    }

    val queueDataText = Source.fromInputStream(queueDataS3Object.content).mkString
    val parsedStuff = Json.fromJson[Seq[QueueData]](Json.parse(queueDataText)).fold(x=>throw new Exception(), x=>x)
    parsedStuff

  }

}
