import db._
import org.junit.runner._
import org.specs2.mock.Mockito
import org.specs2.mutable._
import org.specs2.runner._
import play.Logger
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification with Mockito{

  "Application" should {

    "addData returns 400 and message 'failure' with bad json" in new WithApplication {
      val home = route(FakeRequest(POST, "/adddata").withJsonBody(Json.obj(
        "email" -> "fa...@email.pl",
        "password" -> "fakepassword"
      ))).get

      status(home) must equalTo(400)
      contentType(home) must beSome.which(_ == "application/json")
      contentAsString(home) must contain("message")
      contentAsString(home) must contain("failure")
    }

    "addData returns 200 and message 'success' with good json" in new WithApplication {
      val home = route(FakeRequest(POST, "/adddata").withJsonBody(Json.obj(
        "data" -> "foobar",
        "time" -> "2015-01-01"
      ))).get

      status(home) must equalTo(200)
      contentType(home) must beSome.which(_ == "application/json")
      contentAsString(home) must contain("message")
      contentAsString(home) must contain("success")
    }
  }
}
