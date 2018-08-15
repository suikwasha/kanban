package models.silhouette

import com.mohiva.play.silhouette.api.Identity
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.{ExecutionContext, Future}

case class UserId(value: Long)

case class User(id: UserId, name: String, email: Option[String], providerId: String, providerKey: String) extends Identity

class UserRepository @Inject()(
  protected val dbConfigProvider: DatabaseConfigProvider
)(
  implicit ec: ExecutionContext
) extends HasDatabaseConfigProvider[JdbcProfile] {

  import UserRepository._
  import profile.api._

  def find(providerKey: String, providerId: String): Future[Option[User]] = db.run {
    TableQuery[Users]
      .filter(u => u.providerKey === providerKey && u.providerId === providerId)
      .result
      .headOption
  }.map(_.map(_.toUser))

  def find(name: String): Future[Option[User]] = db.run {
    TableQuery[Users]
      .filter(u => u.name === name).result.headOption
  }.map(_.map(_.toUser))

  def find(id: UserId): Future[Option[User]] = db.run {
    TableQuery[Users]
      .filter(u => u.id === id.value).result.headOption
  }.map(_.map(_.toUser))

  def create(name: String, email: Option[String], providerId: String, providerKey: String): Future[User] = db.run {
    val users = TableQuery[Users]
    val insert = users.returning(users.map(_.id)).into((user, id) => user.copy(id = id))
    insert += SlickUser(0, name, email, CredentialsProvider.ID, providerKey)
  }.map(_.toUser)

}

object UserRepository {

  case class SlickUser(id: Long, name: String, email: Option[String], providerId: String, providerKey: String) {

    def toUser: User = User(UserId(id), name, email, providerId, providerKey)
  }

  object SlickUser {

    def fromUser(from: User): SlickUser = new SlickUser(from.id.value, from.name, from.email, from.providerId, from.providerKey)

    def tupled = (SlickUser.apply _).tupled
  }

  class Users(tag: Tag) extends Table[SlickUser](tag, "USER") {

    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def name = column[String]("NAME", O.Unique)

    def email = column[Option[String]]("EMAIL")

    def providerId = column[String]("PROVIDER_ID")

    def providerKey = column[String]("PROVIDER_KEY")

    def * = (id, name, email, providerId, providerKey) <> (SlickUser.tupled, SlickUser.unapply)
  }
}

