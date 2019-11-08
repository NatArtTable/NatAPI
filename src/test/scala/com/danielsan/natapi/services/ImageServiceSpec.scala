package com.danielsan.natapi.services

import java.nio.file.Paths

import com.danielsan.natapi.helpers.FileHandler
import com.danielsan.natapi.models._
import com.danielsan.natapi.repositories.FileRepositoryImpl
import com.danielsan.natapi.resources.AuthResource.Payload
import com.danielsan.natapi.resources.ImageResources
import org.scalamock.scalatest.MockFactory
import org.scalatest.OneInstancePerTest

import scala.concurrent.Await
import scala.concurrent.duration._

class ImageServiceSpec extends BaseSpec with MockFactory with OneInstancePerTest {
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

  describe("test create service for images") {
    it("test create service for image with correct parameters") {
      val file = stub[FileHandler]

      (file.fileType _).when().returns(FileHandler.JPEG)
      (file.getFilePath _).when().returns(Paths.get("tmpFile"))

      val cloudinary = mockCloudinaryUpload("http://public/url")
      server.fileRepository.asInstanceOf[FileRepositoryImpl].cloudinary = cloudinary

      val newImage = ImageResources.Create(file, Some("description"), Some(Seq("tag1", "tag2", "tag3")))
      val result = Await.result(server.imageService.create(newImage)(Payload(jujuba)), 5.second)

      assert(result.isLeft)
    }

    it("test create service for image with correct parameters and then get the description") {
      val file = stub[FileHandler]

      (file.fileType _).when().returns(FileHandler.PNG)
      (file.getFilePath _).when().returns(Paths.get("tmpFile"))

      val cloudinary = mockCloudinaryUpload("http://public/url")
      server.fileRepository.asInstanceOf[FileRepositoryImpl].cloudinary = cloudinary

      val newImage = ImageResources.Create(file, Some("description"), Some(Seq("tag1", "tag2", "tag3")))
      val result = Await.result(server.imageService.create(newImage)(Payload(jujuba)), 5.second)

      assert(result.isLeft)

      val created = result.left.get.id

      val getCreatedImage = Await.result(server.imageService.getById(created)(Payload(jujuba)), 5.second).left.get
      assert(getCreatedImage.description == newImage.description.get)

    }

    it("test create service for image with correct parameters and no description then get the description") {
      val file = stub[FileHandler]

      (file.fileType _).when().returns(FileHandler.PNG)
      (file.getFilePath _).when().returns(Paths.get("tmpFile"))

      val cloudinary = mockCloudinaryUpload("http://public/url")
      server.fileRepository.asInstanceOf[FileRepositoryImpl].cloudinary = cloudinary

      val newImage = ImageResources.Create(file, None, Some(Seq("tag1", "tag2", "tag3")))
      val result = Await.result(server.imageService.create(newImage)(Payload(jujuba)), 5.second)

      assert(result.isLeft)

      val created = result.left.get.id

      val getCreatedImage = Await.result(server.imageService.getById(created)(Payload(jujuba)), 5.second).left.get
      assert(getCreatedImage.description == "")
    }

    it("test create service for image with correct parameters and get tags") {
      val file = stub[FileHandler]

      (file.fileType _).when().returns(FileHandler.PNG)
      (file.getFilePath _).when().returns(Paths.get("tmpFile"))

      val cloudinary = mockCloudinaryUpload("http://public/url")
      server.fileRepository.asInstanceOf[FileRepositoryImpl].cloudinary = cloudinary

      val newImage = ImageResources.Create(file, Some("description"), Some(Seq("tag1", "tag2", "tag3")))
      val result = Await.result(server.imageService.create(newImage)(Payload(jujuba)), 5.second)

      assert(result.isLeft)

      val created = result.left.get.id

      val getCreatedImage = Await.result(server.imageService.getById(created)(Payload(jujuba)), 5.second).left.get
      assert(getCreatedImage.tags == newImage.tags.get)

    }

    it("test create service for image with correct parameters and no tags then get tags") {
      val file = stub[FileHandler]

      (file.fileType _).when().returns(FileHandler.PNG)
      (file.getFilePath _).when().returns(Paths.get("tmpFile"))

      val cloudinary = mockCloudinaryUpload("http://public/url")
      server.fileRepository.asInstanceOf[FileRepositoryImpl].cloudinary = cloudinary

      val newImage = ImageResources.Create(file, Some("xablau"), None)
      val result = Await.result(server.imageService.create(newImage)(Payload(jujuba)), 5.second)

      assert(result.isLeft)

      val created = result.left.get.id

      val getCreatedImage = Await.result(server.imageService.getById(created)(Payload(jujuba)), 5.second).left.get
      assert(getCreatedImage.tags == Seq())
    }

    it("test create service for image with correct parameters correctly saves the image") {
      val file = stub[FileHandler]

      (file.fileType _).when().returns(FileHandler.PNG)
      (file.getFilePath _).when().returns(Paths.get("tmpFile"))

      val cloudinary = mockCloudinaryUpload("http://public/url")
      server.fileRepository.asInstanceOf[FileRepositoryImpl].cloudinary = cloudinary

      val newImage = ImageResources.Create(file, None, Some(Seq("tag1", "tag2", "tag3")))
      val result = Await.result(server.imageService.create(newImage)(Payload(jujuba)), 5.second)

      assert(result.isLeft)

      val created = result.left.get.id

      val getCreatedImage = Await.result(server.imageService.getById(created)(Payload(jujuba)), 5.second).left.get
      assert(getCreatedImage.public_uri == "http://public/url")
    }

    it("should return a InvalidParamtersException if a no supported content type is passed") {
      val file = stub[FileHandler]

      (file.fileType _).when().returns(FileHandler.TXT)
      (file.getFilePath _).when().returns(Paths.get("tmpFile"))

      val cloudinary = mockCloudinaryUpload("http://public/url")
      server.fileRepository.asInstanceOf[FileRepositoryImpl].cloudinary = cloudinary

      val newImage = ImageResources.Create(file, Some("xablau"), Some(Seq("1")))
      val result = Await.result(server.imageService.create(newImage)(Payload(jujuba)), 5.second)

      assert(result.isRight)
      assert(result.right.get.isInstanceOf[Service.InvalidParametersException])
    }

    it("should return the correct public_url after uploading a image") {
      val file = stub[FileHandler]

      (file.fileType _).when().returns(FileHandler.PNG)
      (file.getFilePath _).when().returns(Paths.get("tmpFile"))

      val cloudinary = mockCloudinaryUpload("http://public/url")
      server.fileRepository.asInstanceOf[FileRepositoryImpl].cloudinary = cloudinary

      val newImage = ImageResources.Create(file, Some("xablau"), Some(Seq("1")))
      val result = Await.result(server.imageService.create(newImage)(Payload(jujuba)), 5.second)

      assert(result.isLeft)
      assert(result.left.get.public_uri == "http://public/url")
    }
  }
}
