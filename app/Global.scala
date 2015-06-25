import java.sql.Connection

import _root_.db._
import awscala._
import awscala.s3.S3
import play.api.Play.current
import play.api._
import play.api.db.DB
import play.api.libs.concurrent.Akka
import upload.{S3Downloader, S3Uploader}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
 * Created by bryan on 5/20/15.
 */
object Global extends GlobalSettings {

  override def onStart(app: Application): Unit = {
    implicit val s3 = S3(Play.current.configuration.getString("streamchannel.aws.clientKey").get,
      Play.current.configuration.getString("streamchannel.aws.clientSecret").get)(Region.US_EAST_1)
    Logger.info(Play.current.configuration.getString("db.default.url").get)
    // todo: figure out the appropriate abstraction to avoid the duplicated code here.

    if (Play.current.mode != Mode.Test) {
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

      // schedules pagedata check
      Akka.system.scheduler.schedule(0.microsecond, 5.second) {
        DB.withTransaction {
          implicit conn: Connection =>
            val referrerDataRepo = new PageDataRepository()
            val s3downloader = new S3Downloader()
            val data = s3downloader.GetPageData()
            referrerDataRepo.UpdateData(data)
        }
      }

      // schedules website check
      Akka.system.scheduler.schedule(0.microsecond, 5.second) {
        DB.withTransaction {
          implicit conn: Connection =>
            val referrerDataRepo = new WebsiteRepository()
            val s3downloader = new S3Downloader()
            val data = s3downloader.GetWebsites()
            referrerDataRepo.UpdateData(data)
        }
      }

      // schedules queue upload check
      Akka.system.scheduler.schedule(0.microsecond, 5.second) {
        DB.withTransaction {
          implicit conn: Connection =>
            val queueDataRepository = new DataEventRepository()
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

}