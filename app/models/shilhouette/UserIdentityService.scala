package models.shilhouette

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class UserIdentityService @Inject()(
  val dbConfigProvider: DatabaseConfigProvider,
  hasher: PasswordHasher,
  authInfoRepository: AuthInfoRepository
)(
  implicit ec: ExecutionContext
) extends IdentityService[User]
  with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = db.run {
    TableQuery[Users]
      .filter(u => u.providerKey === loginInfo.providerKey && u.providerId === loginInfo.providerID)
      .result
      .headOption
  }

  def create(name: String, password: String): Future[User] = db.run {
    val users = TableQuery[Users]
    val insert = users.returning(users.map(_.id)).into((user, id) => user.copy(id = id))
    insert += User(0, name, CredentialsProvider.ID, name)
  }.andThen {
    case Success(user) => {
      authInfoRepository.add(
        LoginInfo(CredentialsProvider.ID, user.name),
        hasher.hash(password)
      )
    }
  }
}
