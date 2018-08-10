package models.shilhouette

import com.mohiva.play.silhouette.api.Identity
import slick.jdbc.MySQLProfile.api._

case class User(id: Int, name: String, providerId: String, providerKey: String) extends Identity

class Users(tag: Tag) extends Table[User](tag, "USER") {

  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

  def name = column[String]("NAME", O.Unique)

  def providerId = column[String]("PROVIDER_ID")

  def providerKey = column[String]("PROVIDER_KEY")

  def * = (id, name, providerId, providerKey) <> (User.tupled, User.unapply)
}
