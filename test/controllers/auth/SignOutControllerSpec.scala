package controllers.auth

import models.silhouette.UserIdentityService
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.WithTestApplication

import scala.concurrent.Future

class SignOutControllerSpec extends AsyncWordSpec with BeforeAndAfterAll with WithTestApplication {

  "SignOutController" should {

    "able to signout" in {
      val userService = injector.instanceOf[UserIdentityService]
      val signIn = injector.instanceOf[SignInController]
      val signInRequest = FakeRequest(POST, "/").withFormUrlEncodedBody(
        "name" -> "foo",
        "password" -> "bar"
      )

      val target = injector.instanceOf[SignOutController]

      for {
        _ <- userService.create("foo", None, "bar")
        signInResult <- signIn.post(signInRequest)
        signOutRequest <- Future.successful(FakeRequest(GET, "/").withCookies(signInResult.newCookies:_*))
        signOutResult <- target.signOut(signOutRequest)
      } yield {
        assert(signOutResult.header.status == 303)
      }
    }
  }
}
