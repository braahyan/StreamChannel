import java.sql.Connection

import db.QueueDataRepository
import org.joda.time.DateTime
import play.api.Play.current
import play.api._
import play.api.db.DB
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import upload.S3Uploader

import scala.concurrent.duration._


/**
 * Created by bryan on 5/20/15.
 */
object Global extends GlobalSettings {
  override def onStart(app: Application): Unit = {
    var lastPurgeTime = DateTime.now()
    val myActor = Akka.system.scheduler.schedule(0.microsecond, 5.second) {
      DB.withTransaction {
        implicit conn: Connection =>
          val queueDataRepository = new QueueDataRepository()
          val queueLength = queueDataRepository.getQueueLength()
          val s3Uploader = new S3Uploader()
          if (queueLength > 0 &&
            (queueLength > 1000 || ((DateTime.now().getMillis - lastPurgeTime.getMillis) > 5000))) {
            s3Uploader.UploadQueueEntries(queueDataRepository.getDataFromQueue())
            queueDataRepository.purgeDataFromQueue()
            lastPurgeTime = DateTime.now()
          }
      }
    }
  }

}