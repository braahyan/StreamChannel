import play.api._
import play.api.Play.current
/**
 * Created by bryan on 5/20/15.
 */
object Global extends GlobalSettings {
    override def onStart(app:Application): Unit ={
      Logger.info("i started up")
      Logger.info(Play.configuration.getString("db.default.url").fold("No db config")(str=>str))
      Logger.info(Play.application.path.getAbsolutePath.toString)
      Logger.info(Play.application.mode.toString)
    }
}
