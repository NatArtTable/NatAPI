package com.danielsan.natapi

import com.cloudinary.{Cloudinary, Uploader}
import com.cloudinary.response.UploadResponse

import scala.concurrent.{Await, Future}
import slick.jdbc.MySQLProfile.api._
import com.danielsan.natapi.models._
import com.danielsan.natapi.repositories.DatabaseModels
import org.scalamock.scalatest.MockFactory
import org.slf4j.LoggerFactory

import scala.concurrent.duration._

class TestServer(implicit val atMost: Duration) extends Implementation with MockFactory {
  private val log = LoggerFactory.getLogger(this.getClass)

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

  def tearDown(): Unit = {
    log.info("Tearing down database:")

    log.info("Tearing down images tables")
    Await.result(database.run(DatabaseModels.images.schema.dropIfExists), atMost)

    log.info("Tearing down users tables")
    Await.result(database.run(DatabaseModels.users.schema.dropIfExists), atMost)
  }
}
