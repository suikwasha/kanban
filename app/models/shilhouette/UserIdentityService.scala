package models.shilhouette

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.api.util.PasswordHasher
import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

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
}
