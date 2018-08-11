package models.shilhouette

import slick.jdbc.MySQLProfile.api._

case class Password(key: String, hasher: String, hash: String, salt: Option[String])

class Passwords(tag: Tag) extends Table[Password](tag, "PASSWORD") {

  def key = column[String]("PROVIDER_KEY", O.PrimaryKey)

  def hasher = column[String]("HASHER")

  def hash = column[String]("HASH")

  def salt = column[Option[String]]("SALT")

  def * = (key, hasher, hash, salt) <> (Password.tupled, Password.unapply)
}
