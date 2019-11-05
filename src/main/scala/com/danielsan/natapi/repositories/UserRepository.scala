package com.danielsan.natapi.repositories

import com.danielsan.natapi.models.User

import scala.concurrent.Future

trait UserRepository extends Repository {
  def getByEmail(email: String): Future[Option[User]]
  def getById(id: Long): Future[Option[User]]
}
