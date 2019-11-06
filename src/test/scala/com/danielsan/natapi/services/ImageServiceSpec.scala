package com.danielsan.natapi.services

import com.danielsan.natapi.models._
import com.danielsan.natapi.resources.AuthResource.Payload
import com.danielsan.natapi.resources.ImageResources

import scala.concurrent.Await
import scala.concurrent.duration._

class ImageServiceSpec extends BaseSpec {
  var daniel: User = _
  var danielImage: Image = _
  var danielOtherImage: Image = _

  var jujuba: User = _
  var jujubaImage: Image = _

  before {
    server.prepare()

    daniel = server.addUser(User(-1, "daniel", "daniel@mail.com", "1234"))
    jujuba = server.addUser(User(-1, "jujuba", "jujuba@mail.com", "4321"))

    danielImage = server.addImage(Image(-1, "descricao", Seq("tag1", "tag2"), "some/place.jpg", "uri", daniel.id))
    danielOtherImage = server.addImage(Image(-1, "outra descricao", Seq("tag134", "tag2"), "another/place.jpg", "loko", daniel.id))
    jujubaImage = server.addImage(Image(-1, "better description", Seq("tag1", "other_tag"), "other/place.jpg", "random_string", jujuba.id))
  }

  describe("test getById service for images") {
    it("should return the correct information of a image if the owner of the images request it") {
      val result = Await.result(server.imageService.getById(danielImage.id)(Payload(daniel)), 5.seconds)

      assert(result.isLeft)
      assert(result.left.get == ImageResources.Full(danielImage.id, "uri", "descricao", Seq("tag1", "tag2")))
    }

    it("should return a PermissionDeniedException if a user tries to get a image of another user") {
      val result = Await.result(server.imageService.getById(jujubaImage.id)(Payload(daniel)), 5.seconds)

      assert(result.isRight)
      assert(result.right.get.isInstanceOf[Service.PermissionDeniedException])
    }

    it("should return a NotFoundException if a user tries to access a non existing image") {
      val result = Await.result(server.imageService.getById(42)(Payload(daniel)), 5.seconds)

      assert(result.isRight)
      assert(result.right.get.isInstanceOf[Service.NotFoundException])
    }
  }

  describe("test getAll service for images") {
    it("should returns only the two images (with small resource) belonging to daniel if daniel requests it") {
      val result = Await.result(server.imageService.getAll()(Payload(daniel)), 5.second)

      assert(result.isLeft)
      assert(result.left.get.toSet == Set(ImageResources.Small(danielImage), ImageResources.Small(danielOtherImage)))
    }

    it("should returns only the one image (with small resource) belonging to jujuba if jujuba requests it") {
      val result = Await.result(server.imageService.getAll()(Payload(jujuba)), 5.second)

      assert(result.isLeft)
      assert(result.left.get.toSet == Set(ImageResources.Small(jujubaImage)))
    }
  }
}
