import java.sql.Connection

import _root_.db.{VisitDataRepository, QueueDataRepository, ReferrerDataRepository}
import awscala._
import awscala.s3.S3
import play.api._
import play.api.db.DB
import play.api.libs.concurrent.Akka
import upload.{S3Downloader, S3Uploader}
import play.api.Play.current
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by bryan on 5/20/15.
 */
object Global extends GlobalSettings {

  override def onStart(app: Application): Unit = {
    implicit val s3 = S3(Play.current.configuration.getString("streamchannel.aws.clientKey").get,
      Play.current.configuration.getString("streamchannel.aws.clientSecret").get)(Region.US_EAST_1)

    // schedules referrerdata check
    Akka.system.scheduler.schedule(0.microsecond, 5.second) {
      DB.withTransaction {
        implicit conn: Connection =>
          val referrerDataRepo = new ReferrerDataRepository()
          val s3downloader = new S3Downloader()
          val data = s3downloader.GetReferrerData()
          referrerDataRepo.UpdateData(data)
      }
    }

    // schedules visitordata check
    Akka.system.scheduler.schedule(0.microsecond, 5.second) {
      DB.withTransaction {
        implicit conn: Connection =>
          val referrerDataRepo = new VisitDataRepository()
          val s3downloader = new S3Downloader()
          val data = s3downloader.GetVisitorData()
          referrerDataRepo.UpdateData(data)
      }
    }

    // schedules queue upload check
    Akka.system.scheduler.schedule(0.microsecond, 5.second) {
      DB.withTransaction {
        implicit conn: Connection =>
          val queueDataRepository = new QueueDataRepository()
          val queueLength = queueDataRepository.getQueueLength()
          val s3Uploader = new S3Uploader()
          if (queueLength > 0) {
            s3Uploader.UploadQueueEntries(queueDataRepository.getDataFromQueue())
            queueDataRepository.purgeDataFromQueue()
          }
      }
    }
  }

}