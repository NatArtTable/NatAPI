package com.danielsan.natapi.resources

import com.danielsan.natapi.models.User

object UserResource {
  trait Generic

  case class Public(name: String) extends Generic
  object Public {
    def apply(user: User): Public = Public(user.name)
  }

  case class Private(name: String, email: String) extends Generic
  object Private {
    def apply(user: User): Private = Private(user.name, user.email)
  }
}
