package test
import awscala._
import awscala.s3.S3
import db.{DataEvent, DataEvent$}
import org.joda.time.DateTime
import org.specs2.mutable._
import play.api.Play
import play.api.libs.json.JsString
import play.api.test._
import upload.S3Uploader


/**
 * Created by bryan on 5/29/15.
 */
class S3UploaderSpec extends Specification{
  "should upload to s3" in new WithApplication {
    implicit val s3 = S3(Play.current.configuration.getString("streamchannel.aws.clientKey").get,
      Play.current.configuration.getString("streamchannel.aws.clientSecret").get)(Region.US_EAST_1)
    val s3uploader = new S3Uploader()
    val queueEntries = Seq(DataEvent(JsString("fooooo"), Some(DateTime.now())))
    val key = s3uploader.UploadQueueEntries(queueEntries)
    val rehydratedEntries = s3uploader.GetQueueEntries(key)
    queueEntries must beEqualTo(rehydratedEntries)
  }
}
