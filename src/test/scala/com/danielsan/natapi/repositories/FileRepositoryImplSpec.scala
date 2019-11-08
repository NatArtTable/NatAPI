package com.danielsan.natapi.repositories

import java.nio.file.Paths

import com.cloudinary.response.UploadResponse
import com.cloudinary.{Cloudinary, Uploader}

import scala.concurrent.duration._
import com.danielsan.natapi.helpers.FileHandler
import com.danielsan.natapi.services.BaseSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.OneInstancePerTest

import scala.concurrent.{Await, Future}

class FileRepositoryImplSpec extends BaseSpec with MockFactory with OneInstancePerTest {

  implicit val cloudinary = stub[Cloudinary]
  val fileRepository = new FileRepositoryImpl()

  describe("Testing method save") {
    it("Should upload the correct file to cloudinary") {
      val file = stub[FileHandler]
      (file.getFilePath _).when().returns(Paths.get("/Somewhere", "over", "the", "rainbow"))

      val cloudinary = mockCloudinaryUpload("public/uri", "/Somewhere/over/the/rainbow")
      fileRepository.asInstanceOf[FileRepositoryImpl].cloudinary = cloudinary

      val result = Await.result(fileRepository.save(file), 5.seconds)

      assert(result == "public/uri")
    }
  }
}
