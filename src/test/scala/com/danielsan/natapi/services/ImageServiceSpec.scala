package com.danielsan.natapi.services

import com.danielsan.natapi.models._
import com.danielsan.natapi.resources.AuthResource.Payload
import com.danielsan.natapi.resources.ImageResource

import scala.concurrent.Await
import scala.concurrent.duration._

class ImageServiceSpec extends BaseSpec {
  var danielImageId = -1L
  var danielId = -1L

  var jujubaImageId = -1L
  var jujubaId = -1L

  before {
    server.prepare()

    danielId = server.addUser(User(-1, "daniel", "daniel@mail.com", "1234"))
    jujubaId = server.addUser(User(-1, "jujuba", "jujuba@mail.com", "4321"))
    "some/place.jpg"
    danielImageId = server.addImage(Image(-1, "descricao", Seq("tag1", "tag2"), "some/place.jpg", "uri", danielId))
    jujubaImageId = server.addImage(Image(-1, "better description", Seq("tag1", "other_tag"), "other/place.jpg", "uri", jujubaId))
  }

  describe("test getById service for images") {
    it("should return the correct information of a image if the owner of the images request it") {
      val result = Await.result(server.impl.imageService.getById(danielImageId)(Payload(danielId, "daniel@mail.com")), 5.seconds)

      assert(result.isLeft)
      assert(result.left.get == ImageResource.Full(danielImageId, "uri", "descricao", Seq("tag1", "tag2")))
    }

    it("should return a PermissionDeniedException if a user tries to get a image of another user") {
      val result = Await.result(server.impl.imageService.getById(jujubaImageId)(Payload(danielId, "daniel@mail.com")), 5.seconds)

      assert(result.isRight)
      assert(result.right.get.isInstanceOf[Service.PermissionDeniedException])
    }

    it("should return a NotFoundException if a user tries to access a non existing image") {
      val result = Await.result(server.impl.imageService.getById(42)(Payload(danielId, "daniel@mail.com")), 5.seconds)

      assert(result.isRight)
      assert(result.right.get.isInstanceOf[Service.NotFoundException])
    }
  }
}
