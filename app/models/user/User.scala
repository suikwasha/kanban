package models.user

import models.user.UserRepository.UserRepositoryError

case class UserId(value: Long)

case class User(id: UserId, name: String)

class UserRepository {

  def create(name: String): Either[UserRepositoryError, User] = {

  }

  def find(id: UserId): Either[UserRepositoryError, Option[User]] = {
    Right(None)
  }

  def store(user: User): Either[UserRepositoryError, Unit] = {

  }

  def remove(id: UserId): Either[UserRepositoryError, Boolean] = {

  }

  def all: Either[UserRepositoryError, Seq[User]] = {

  }

}

object UserRepository {

  sealed trait UserRepositoryError

  object UserRepositoryErrors {


  }
}
