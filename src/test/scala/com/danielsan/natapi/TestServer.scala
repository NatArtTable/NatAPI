package com.danielsan.natapi

import scala.concurrent.{ExecutionContext, Await}
import slick.jdbc.MySQLProfile.api._

import com.danielsan.natapi.models._
import com.danielsan.natapi.repositories.DatabaseModels

import scala.concurrent.duration._

class TestServer(val atMost: Duration) {
  implicit val database: Database = Database.forConfig("test_db")

  val impl = new Implementation(database, "/tmp/natapi", atMost)

  def addUser(user: User): User = {
    val id = Await.result(database.run((DatabaseModels.users returning DatabaseModels.users.map(_.id)) += user), atMost)
    val newUser = user.copy(id = id)

    newUser
  }

  def addImage(image: Image): Image = {
    val id = Await.result(database.run((DatabaseModels.images returning DatabaseModels.images.map(_.id)) += image), atMost)
    val newImage = image.copy(id = id)

    newImage
  }

  def prepare(): Unit = {
    impl.prepare()
  }

  def tearDown(): Unit = {
    Await.result(database.run(DatabaseModels.images.schema.dropIfExists), atMost)
    Await.result(database.run(DatabaseModels.users.schema.dropIfExists), atMost)
  }
}
