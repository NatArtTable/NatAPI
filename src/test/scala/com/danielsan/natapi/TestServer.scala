package com.danielsan.natapi

import scala.concurrent.{ExecutionContext, Await}
import slick.jdbc.MySQLProfile.api._

import com.danielsan.natapi.models._
import com.danielsan.natapi.repositories.DatabaseModels

import scala.concurrent.duration._

class TestServer(val atMost: Duration) {
  implicit val database: Database = Database.forConfig("test_db")

  val impl = new Implementation(database, "/tmp", atMost)

  def addUser(user: User): Long = {
    Await.result(database.run((DatabaseModels.users returning DatabaseModels.users.map(_.id)) += user), atMost)
  }

  def addImage(image: Image): Long = {
    Await.result(database.run((DatabaseModels.images returning DatabaseModels.images.map(_.id)) += image), atMost)
  }

  def prepare(): Unit = {
    impl.prepare()
  }

  def tearDown(): Unit = {
    Await.result(database.run(DatabaseModels.images.schema.dropIfExists), atMost)
    Await.result(database.run(DatabaseModels.users.schema.dropIfExists), atMost)
  }
}
