package controllers.auth

import models.silhouette.UserIdentityService
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, BeforeAndAfterAll}
import play.api.test.FakeRequest
import utils.WithTestApplication
import play.api.test.Helpers._

class SignInControllerSpec extends AsyncWordSpec with BeforeAndAfterAll with WithTestApplication {

  "SignInController" should {

    "able to sign in with valid user name and password" in {
      val userService = injector.instanceOf[UserIdentityService]
      val target = injector.instanceOf[SignInController]
      val req = FakeRequest(POST, "/").withFormUrlEncodedBody(
        "name" -> "foo",
        "password" -> "bar"
      )

      for {
        _ <- userService.create("foo", None, "bar")
        result <- target.post(req)
      } yield {
        assert(result.header.status == 303)
        assert(result.newCookies.nonEmpty)
      }
    }

    "unable to sign in with wrong password" in {
      val userService = injector.instanceOf[UserIdentityService]
      val target = injector.instanceOf[SignInController]
      val req = FakeRequest(POST, "/").withFormUrlEncodedBody(
        "name" -> "foo2",
        "password" -> "bar2"
      )

      for {
        _ <- userService.create("foo2", None, "bar1")
        result <- target.post(req)
      } yield {
        assert(result.header.status == 303)
        assert(result.newCookies.isEmpty)
      }

    }

    "unable to sign in with invalid user and password" in {
      val userService = injector.instanceOf[UserIdentityService]
      val target = injector.instanceOf[SignInController]
      val req = FakeRequest(POST, "/").withFormUrlEncodedBody(
        "name" -> "fuga",
        "password" -> "hoge"
      )

      for {
        _ <- userService.create("hoge", None, "fuga")
        result <- target.post(req)
      } yield {
        assert(result.header.status == 303)
        assert(result.newCookies.isEmpty)
      }

    }
  }
}
