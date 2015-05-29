import DB.QueueDataRepository
import org.joda.time.DateTime
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.db.DB
import play.api.test._


@RunWith(classOf[JUnitRunner])
class QueueDataRepositorySpec extends Specification {


  "no errors should be thrown when inserting into an empty queue" in new WithApplication {
    DB.withTransaction {
      implicit conn =>
        val queueDataRepostiory = new QueueDataRepository
        val id = queueDataRepostiory.addDataToQueue(new DateTime(), "")
        id must beSome[Long]
        id must beSome(1)
    }
  }

  "inserting data into an empty queue should allow that data to be retrieved" in new WithApplication {
    DB.withTransaction {
      implicit conn =>
        val queueDataRepostiory = new QueueDataRepository
        val queue = queueDataRepostiory.getDataFromQueue()
        queue mustEqual(Nil)
        val date = new DateTime()
        val data = "fooobar"
        queueDataRepostiory.addDataToQueue(date, data)
        val newData = queueDataRepostiory.getDataFromQueue()
        newData.length mustEqual(1)
        newData(0) mustEqual((date, data))
    }
  }

  "purging the queue should ensure that the queue is empty" in new WithApplication {
    DB.withTransaction {
      implicit conn =>
        val queueDataRepostiory = new QueueDataRepository
        queueDataRepostiory.addDataToQueue(new DateTime(), "")
        queueDataRepostiory.addDataToQueue(new DateTime(), "")
        val checkData = queueDataRepostiory.getDataFromQueue()
        checkData.length mustEqual(2)
        queueDataRepostiory.purgeDataFromQueue()
        val queueData = queueDataRepostiory.getDataFromQueue()
        queueData mustEqual(Nil)
    }
  }

}
