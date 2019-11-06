package com.danielsan.natapi.repositories

import scala.concurrent.{Future, ExecutionContext}
import ExecutionContext.Implicits.global

import slick.jdbc.MySQLProfile.api._

import DatabaseModels.UserRow
import com.danielsan.natapi.models.User

class UserRepositoryImpl(implicit val db: Database, implicit val users: TableQuery[UserRow]) extends UserRepository {

  def prepare(): Future[Unit] = {
    db.run(users.schema.create)
  }

  def getById(id: Long): Future[Option[User]] = {
    db.run(users.filter(_.id === id).result.map(_.headOption)).map {
      case Some(row) => Some(User(row.id, row.name, row.email, row.password))
      case None      => None
    }
  }

  def getAll(): Future[Seq[User]] = {
    db.run(users.result).map { rows =>
      rows.map(row => User(row.id, row.name, row.email, row.password))
    }
  }

  def getByEmail(email: String): Future[Option[User]] = {
    db.run(users.filter(_.email === email).result.map(_.headOption)).map {
      case Some(row) => Some(User(row.id, row.name, row.email, row.password))
      case None      => None
    }
  }
}
