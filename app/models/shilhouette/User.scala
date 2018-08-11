package models.shilhouette

import com.mohiva.play.silhouette.api.Identity
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Future

case class User(id: Long, name: String, providerId: String, providerKey: String) extends Identity

class UserRepository @Inject()(
  protected val dbConfigProvider: DatabaseConfigProvider
) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def find(providerKey: String, providerId: String): Future[Option[User]] = db.run {
    TableQuery[Users]
      .filter(u => u.providerKey === providerKey && u.providerId === providerId)
      .result
      .headOption
  }

  def create(name: String, providerId: String, providerKey: String): Future[User] = db.run {
    val users = TableQuery[Users]
    val insert = users.returning(users.map(_.id)).into((user, id) => user.copy(id = id))
    insert += User(0, name, CredentialsProvider.ID, providerKey)
  }

}

class Users(tag: Tag) extends Table[User](tag, "USER") {

  def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

  def name = column[String]("NAME", O.Unique)

  def providerId = column[String]("PROVIDER_ID")

  def providerKey = column[String]("PROVIDER_KEY")

  def * = (id, name, providerId, providerKey) <> (User.tupled, User.unapply)
}
