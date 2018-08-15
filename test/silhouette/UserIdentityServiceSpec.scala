package silhouette

import models.silhouette.{UserIdentityService, UserRepository}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll}
import utils.WithTestApplication

class UserIdentityServiceSpec extends AsyncWordSpec with BeforeAndAfterAll with WithTestApplication {

  "UserIdentityService" should {

    "able to create user" in {
      val repo = injector.instanceOf[UserRepository]
      val service = injector.instanceOf[UserIdentityService]
      for {
        newUser <- service.create("foo", None, "hoge")
        foundUser <- repo.find(newUser.id)
      } yield {
        assert(foundUser.contains(newUser))
      }
    }
  }
}
