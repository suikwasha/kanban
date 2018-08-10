package models.shilhouette

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class PasswordInfoRepository @Inject()(val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends DelegableAuthInfoDAO[PasswordInfo]
  with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  override def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = db.run {
    TableQuery[Passwords]
      .filter(_.key === loginInfo.providerKey).result.headOption
      .map(_.map(p => PasswordInfo(p.hasher, p.hash, p.salt)))
  }

  override def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = ???

  override def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = ???

  override def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = ???

  override def remove(loginInfo: LoginInfo): Future[Unit] = ???
}


