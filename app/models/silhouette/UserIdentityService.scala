package models.silhouette

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserIdentityService @Inject()(
  hasher: PasswordHasher,
  userRepository: UserRepository,
  authInfoRepository: AuthInfoRepository
)(
  implicit ec: ExecutionContext
) extends IdentityService[User] {

  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userRepository.find(loginInfo.providerKey, loginInfo.providerID)

  def create(name: String, email: Option[String], password: String): Future[User] = {
    for {
      newUser <- userRepository.create(name, email, CredentialsProvider.ID, name)
      _ <- authInfoRepository.add(LoginInfo(CredentialsProvider.ID, newUser.name), hasher.hash(password))
    } yield newUser
  }

}
