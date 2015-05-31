import db.QueueData
import org.joda.time.DateTime
import org.specs2.mutable._
import play.api.libs.json.JsString
import play.api.test._
import upload.S3Uploader


/**
 * Created by bryan on 5/29/15.
 */
class S3UploaderSpec extends Specification{
  "should upload to s3" in new WithApplication {
    val s3uploader = new S3Uploader()
    val queueEntries = Seq(QueueData(JsString("fooooo"), Some(DateTime.now())))
    val key = s3uploader.UploadQueueEntries(queueEntries)
    val rehydratedEntries = s3uploader.GetQueueEntries(key)
    queueEntries must beEqualTo(rehydratedEntries)

  }
}
