import db.{WebEvent, DataEvent, DataEvent$}
import org.joda.time.DateTime
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.libs.json.JsString
import play.api.test._


@RunWith(classOf[JUnitRunner])
class DataEventSpec extends Specification {


  "DataEvent withTimestamp method should only replace with the passed in value if serverTimestamp is None" in new WithApplication {
    val queueData = DataEvent(WebEvent("",Some(""),"",0,false), None)
    val dateTime = new DateTime()
    val queueData2 = queueData.withTimeStamp(dateTime)
    queueData.data must beEqualTo(queueData2.data)
    Some(dateTime) must beEqualTo(queueData2.serverTime)
  }

  "DataEvent withTimestamp method should not replace if the original value is Some" in new WithApplication {
    val queueData = DataEvent(WebEvent("",Some(""),"",0,false), Some(new DateTime(2015,12,1,12,12)))
    val dateTime = new DateTime(2015,12,1,12,13)
    val queueData2 = queueData.withTimeStamp(dateTime)
    queueData.data must beEqualTo(queueData2.data)
    Some(dateTime) mustNotEqual(queueData2.serverTime)
  }
}
