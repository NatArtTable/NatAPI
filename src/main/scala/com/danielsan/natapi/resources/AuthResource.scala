package com.danielsan.natapi.resources

import com.danielsan.natapi.models.User

object AuthResource {
  case class Token(token: String)
  case class Credential(email: String, password: String)

  case class Payload(id: Long, email: String)

  object Payload {
    def apply(user: User): Payload = Payload(user.id, user.email)
  }
}
