package models.auth

import models.auth.AuthenticationRepository.AuthenticationRepositoryError
import models.user.UserId

case class AuthenticationId(value: Long)

case class Authentication(authenticationId: AuthenticationId, userId: UserId, hashedPassword: String)

class AuthenticationRepository
{

  def create(id: UserId, hashedPassword: String): Either[AuthenticationRepositoryError, Authentication] = {

  }

  def find(id: UserId): Either[AuthenticationRepositoryError, Option[Authentication]] = {
    Right(None)
  }
}

object AuthenticationRepository {

  sealed trait AuthenticationRepositoryError

  object AuthenticationRepositoryErrors {

  }
}

