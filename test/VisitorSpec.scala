package test

import db.{DataEvent, Visitor, WebEvent}
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.test.WithApplication

/**
 * Created by bryan on 6/13/15.
 */
@RunWith(classOf[JUnitRunner])
class VisitorSpec extends Specification {

  "visitor separates sessions appropriately based on time" in new WithApplication {
    val visitor = new Visitor(Seq(
      DataEvent(WebEvent("",Some(""),"",0,false), Some(new DateTime(2015, 6, 13,3,0,0))),
      DataEvent(WebEvent("",Some(""),"",0,false), Some(new DateTime(2015, 6, 13,3,15,0))),
      DataEvent(WebEvent("",Some(""),"",0,false), Some(new DateTime(2015, 6, 13,3,45,30)))
    ))

    val sessions = visitor.getSessions
    println(sessions)
    sessions.length should_== 2
    sessions(0).pageViews should_==2
    sessions(1).pageViews should_==1
  }
}
