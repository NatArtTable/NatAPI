package com.danielsan.natapi.resources

import com.danielsan.natapi.models.User

object UserResource {

  case class Public(name: String, email: String)
  object Public {
    def apply(user: User): Public = Public(user.name, user.email)
  }

  case class Private(id: Long, name: String, email: String)
  object Private {
    def apply(user: User): Private = Private(user.id, user.name, user.email)
  }
}
