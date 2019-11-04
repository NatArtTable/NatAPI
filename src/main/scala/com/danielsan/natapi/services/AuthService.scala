package com.danielsan.natapi.services

import com.danielsan.natapi.helpers.{Crypto, PayloadSerializer}
import com.danielsan.natapi.repositories.UserRepository
import com.danielsan.natapi.resources.AuthResource._
import com.twitter.util.Future

object AuthService {
  sealed abstract class Exception(msg: String) extends scala.Exception(msg)
  class WrongPasswordException(msg: String) extends Exception(msg)
  class UserNotFoundException(msg: String) extends Exception(msg)
  class InvalidPayloadException(msg: String) extends Exception(msg)
}

class AuthService(repository: UserRepository) {
  def login(c: Credential): Future[Either[Token, Exception]] = {

    repository.getByEmail(c.email) map {
      case Some(user) => {
        if (user.password == c.password) {
          val payload = Payload(user)
          val token = Crypto.encrypt(PayloadSerializer.serialize(payload))

          Left(Token(token))
        } else {
          Right(new AuthService.WrongPasswordException("Wrong password!"))
        }
      }
      case None => Right(new AuthService.UserNotFoundException("User not found!"))
    }
  }
}
