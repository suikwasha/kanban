package utils

import controllers.auth.SignInController
import models.silhouette.UserIdentityService
import org.scalatest.AsyncWordSpec
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

trait WithSignedInResult extends AsyncWordSpec {
  this: WithTestApplication =>

  val signInResultFuture: Future[Result] = Future {
    val userService = injector.instanceOf[UserIdentityService]
    val signInController = injector.instanceOf[SignInController]
    val signInRequest = FakeRequest(POST, "/").withFormUrlEncodedBody(
      "name" -> "foo",
      "password" -> "bar"
    )

    for {
      _ <- userService.create("foo", None, "bar")
      result <- signInController.post(signInRequest)
    } yield {
      result
    }
  }.flatten
}
