import java.sql.{DriverManager, Connection}

import DB.QueueDataRepository
import org.joda.time.DateTime
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.db.DB
import play.api.db.evolutions.{Evolutions, Evolution}
import play.api.test.Helpers._
import play.api.test._
import play.api.Play.current

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */



@RunWith(classOf[JUnitRunner])
class QueueDataRepositorySpec extends Specification {


  "test insert" in new WithApplication {
    DB.withTransaction {
      implicit conn =>
        val queueDataRepostiory = new QueueDataRepository
        val id = queueDataRepostiory.addDataToQueue(new DateTime(), "")
        id must beSome[Long]
        id must beSome(1)
    }
      }

}
