import db.{QueueData, QueueDataRepository}
import play.api.libs.json.JsString
import upload.S3Uploader
import org.joda.time.DateTime
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.db.DB
import play.api.test._


/**
 * Created by bryan on 5/29/15.
 */
class S3UploaderSpec extends Specification{
  "should upload to s3" in new WithApplication {
    val s3uploader = new S3Uploader()
    val queueEntries = Seq(QueueData(JsString("fooooo"),DateTime.now(), Some(DateTime.now())))
    val key = s3uploader.UploadQueueEntries(queueEntries)
    val rehydratedEntries = s3uploader.GetQueueEntries(key)
    queueEntries must beEqualTo(rehydratedEntries)

  }
}
