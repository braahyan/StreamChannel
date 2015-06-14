package test
import db.{LandingPageSegment, WebEvent}
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.test.WithApplication

/**
 * Created by bryan on 6/13/15.
 */
@RunWith(classOf[JUnitRunner])
class LandingPageSegmentSpec extends Specification {


  "referrer segment matches correctly" in new WithApplication {
    val segment = LandingPageSegment("Referrer", ".*",true, None)
    segment.matches(
      WebEvent("http://reddit.com",Some("http://thrivehive.com"), "page-view", 1, true)
    ) should_==(true)

    segment.matches(
      WebEvent("http://reddit.com",None, "page-view", 1, true)
    ) should_==(false)
  }

  "document location segment matches correctly" in new WithApplication {
    val segment = LandingPageSegment("Anything", ".*(\\?|\\&)something=foo.*",false, None)
    segment.matches(
      WebEvent("http://reddit.com?something=foo",Some("http://thrivehive.com"), "page-view", 1, true)
    ) should_==(true)

    segment.matches(
      WebEvent("http://reddit.com?asdfasdf=asdf&something=foo",Some("http://thrivehive.com"), "page-view", 1, true)
    ) should_==(true)

    segment.matches(
      WebEvent("http://reddit.com",None, "page-view", 1, true)
    ) should_==(false)
  }
}
