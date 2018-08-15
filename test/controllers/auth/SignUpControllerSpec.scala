package controllers.auth

import models.silhouette.UserRepository
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll}
import play.api.test.FakeRequest
import utils.WithTestApplication
import play.api.test.Helpers._

class SignUpControllerSpec extends AsyncWordSpec with BeforeAndAfterAll with WithTestApplication {

  "SignUpController" should {

    "able to signup" in {
      val target = injector.instanceOf[SignUpController]
      val req = FakeRequest(POST, "/").withFormUrlEncodedBody(
        "name" -> "foo",
        "password" -> "password"
      )

      for {
        result <- target.post.apply(req)
        addedUser <- injector.instanceOf[UserRepository].find("foo")
      } yield {
        assert(result.header.status == 303 && addedUser.exists(u => u.name == "foo" && u.email.isEmpty))
      }
    }

    "badrequest if name is empty" in {
      val target = injector.instanceOf[SignUpController]
      val req = FakeRequest(POST, "/").withFormUrlEncodedBody(
        "password" -> "password"
      )

      for {
        result <- target.post.apply(req)
      } yield {
        assert(result.header.status == 400)
      }
    }
  }
}
