package silhouette

import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.silhouette.UserRepository
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll}
import play.api.db.slick.DatabaseConfigProvider
import utils.WithTestApplication

class UserRepositorySpec extends AsyncWordSpec with BeforeAndAfterAll with WithTestApplication {

  "UserRepository" should {

    "able to create/find user" in {
      val repo = new UserRepository(injector.instanceOf[DatabaseConfigProvider])

      for {
        newUser <- repo.create("hoge", Some("foo@example.com"), "providerId", "providerKey")
        foundUser <- repo.find(newUser.id)
      } yield {
        assert(foundUser.contains(newUser))
      }
    }

    "able to find with provider key and id" in {
      val repo = new UserRepository(injector.instanceOf[DatabaseConfigProvider])

      for {
        newUser <- repo.create("fuga", Some("foo@example.com"), "providerId2", "providerKey2")
        foundUser <- repo.find("providerKey2", CredentialsProvider.ID)
      } yield {
        assert(foundUser.contains(newUser))
      }
    }
  }
}
