package test
import db.{WebEvent, DataEvent, DataEvent$, DataEventRepository}
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.db.DB
import play.api.libs.json.JsString
import play.api.test._


@RunWith(classOf[JUnitRunner])
class DataEventRepositorySpec extends Specification {


  "no errors should be thrown when inserting into an empty queue" in new WithApplication {
    DB.withTransaction {
      implicit conn =>
        val queueDataRepostiory = new DataEventRepository
        val id = queueDataRepostiory.addDataToQueue(DataEvent(WebEvent("",Some(""),"",0,false), None))
        id must beSome[Long]
        id must beSome(1)
    }
  }

  "inserting data into an empty queue should allow that data to be retrieved" in new WithApplication {
    DB.withTransaction {
      implicit conn =>
        val queueDataRepostiory = new DataEventRepository
        val queue = queueDataRepostiory.getDataFromQueue()
        queue mustEqual(Nil)
        val data = DataEvent(WebEvent("",Some(""),"",0,false), None)
        queueDataRepostiory.addDataToQueue(data)
        val newData = queueDataRepostiory.getDataFromQueue()
        newData.length mustEqual(1)
        newData(0) mustEqual(data)
    }
  }

  "purging the queue should ensure that the queue is empty" in new WithApplication {
    DB.withTransaction {
      implicit conn =>
        val queueDataRepostiory = new DataEventRepository
        queueDataRepostiory.addDataToQueue(DataEvent(WebEvent("",Some(""),"",0,false), None))
        queueDataRepostiory.addDataToQueue(DataEvent(WebEvent("",Some(""),"",0,false), None))
        val checkData = queueDataRepostiory.getDataFromQueue()
        checkData.length mustEqual(2)
        val deletedRows = queueDataRepostiory.purgeDataFromQueue()
        deletedRows mustEqual(2)
        val queueData = queueDataRepostiory.getDataFromQueue()
        queueData mustEqual(Nil)
    }
  }

  "count returns appropriate numbers" in new WithApplication {
    DB.withTransaction {
      implicit conn =>
        val queueDataRepostiory = new DataEventRepository
        queueDataRepostiory.addDataToQueue(DataEvent(WebEvent("",Some(""),"",0,false), None))
        queueDataRepostiory.addDataToQueue(DataEvent(WebEvent("",Some(""),"",0,false), None))
        val checkData = queueDataRepostiory.getQueueLength()
        checkData mustEqual(2)
    }
  }

}
