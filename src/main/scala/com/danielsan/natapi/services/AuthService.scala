package com.danielsan.natapi.services

import com.danielsan.natapi.helpers.{Crypto, PayloadSerializer}
import com.danielsan.natapi.repositories.UserRepository
import com.danielsan.natapi.resources.AuthResource._
import com.twitter.util.Future

trait AuthService {
  def login(c: Credential): Future[Either[Token, Service.Exception]]
  def auth(h: String): Either[Payload, Exception]
}

class AuthServiceImpl(implicit val repository: UserRepository) extends AuthService {
  override def login(c: Credential): Future[Either[Token, Service.Exception]] = {

    repository.filter("email", c.email, limit = 1) map (_.headOption) map {
      case Some(user) => {
        if (user.password == c.password) {
          val payload = Payload(user)
          val token = Crypto.encrypt(PayloadSerializer.serialize(payload))

          Left(Token(token))
        } else {
          Right(new Service.PermissionDeniedException("Wrong password!"))
        }
      }
      case None => Right(new Service.NotFoundException("User not found!"))
    }
  }

  override def auth(h: String): Either[Payload, Exception] = {
    if (h == null) Right(new Service.MissingParameterException("Missing payload!"))
    else {
      try {
        val decrypted = Crypto.decrypt(h)
        Left(PayloadSerializer.deserialize(decrypted))
      } catch {
        case e: Exception => Right(new Service.PermissionDeniedException(e.getMessage))
      }
    }
  }
}
