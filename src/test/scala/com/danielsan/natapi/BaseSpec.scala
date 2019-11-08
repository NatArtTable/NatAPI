package com.danielsan.natapi.services

import com.cloudinary.{Cloudinary, Uploader}
import com.cloudinary.response.UploadResponse
import com.danielsan.natapi.TestServer
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfter, FunSpec}

import scala.concurrent.Future
import scala.concurrent.duration._

trait BaseSpec extends FunSpec with BeforeAndAfter with MockFactory {
  private implicit val atMost: Duration = 5.seconds

  protected val server: TestServer = new TestServer()

  after {
    server.tearDown()
  }

  def mockCloudinaryUpload(public_uri: String, expectation: String = null): Cloudinary = {
    val cloudinary: Cloudinary = stub[Cloudinary]

    val uploaderResponse = UploadResponse("123", public_uri, "http://secure/url", "signature", 43254, "image")

    val uploaderMock = if (expectation == null) {
      val uploader = stub[Uploader]
      (uploader.upload _).when(*, *, *, *).returns(Future.successful(uploaderResponse))
      uploader
    } else {
      val uploader = mock[Uploader]
      (uploader.upload _).expects(expectation, *, *, *).returning(Future.successful(uploaderResponse))
      uploader
    }

    (cloudinary.uploader _).when().returns(uploaderMock)

    cloudinary
  }
}
