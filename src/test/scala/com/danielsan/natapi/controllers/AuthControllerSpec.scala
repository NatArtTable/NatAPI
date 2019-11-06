package com.danielsan.natapi.controllers

import com.danielsan.natapi.resources.AuthResource.{Credential, Token}
import com.danielsan.natapi.services.{AuthService, Service}
import com.twitter.finagle.http.Status
import io.finch.Input
import org.scalatest.FunSpec
import io.circe.generic.auto._
import io.finch.circe._
import org.scalamock.scalatest.MockFactory

import scala.concurrent.Future

class AuthControllerSpec extends FunSpec with MockFactory {

  describe("Testing authentication endpoint") {
    it("Should return the user Token with status 200 when POST /auth with valid Credential") {
      implicit val mockedAuthService: AuthService = mock[AuthService]
      val authController = new AuthController

      val credential = Credential("daniel@mail.com", "1234")
      val token = Token("123")

      (mockedAuthService.login _).expects(credential).returning(Future.successful(Left(token)))

      val input = Input.post("/auth").withBody(credential)

      val result = authController.getEndpoints(input).awaitOutput()

      assert(result.isDefined)
      assert(!result.get.isThrow)
      assert(result.get.get.status == Status.Ok)
      assert(result.get.get.value == token)
    }

    it("should returns 404 (user not found) when POST /auth and the service throws NotFoundException") {
      implicit val mockedAuthService: AuthService = mock[AuthService]
      val authController = new AuthController

      val credential = Credential("invalid@mail.com", "1234")
      val e = new Service.NotFoundException("user not found!")

      (mockedAuthService.login _).expects(credential).returning(Future.successful(Right(e)))

      val input = Input.post("/auth").withBody(credential)

      val result = authController.getEndpoints(input).awaitOutput()

      assert(result.isDefined)
      assert(!result.get.isThrow)
      assert(result.get.get.status == Status.NotFound)

      assertThrows[Service.NotFoundException] {
        result.get.get.value
      }
    }

    it("should returns 403 (invalid password) when POST /auth and the service throws PermissionDeniedException") {
      implicit val mockedAuthService: AuthService = mock[AuthService]
      val authController = new AuthController

      val credential = Credential("valid@mail.com", "wrong password")
      val e = new Service.PermissionDeniedException("wrong password!")

      (mockedAuthService.login _).expects(credential).returning(Future.successful(Right(e)))

      val input = Input.post("/auth").withBody(credential)

      val result = authController.getEndpoints(input).awaitOutput()

      assert(result.isDefined)
      assert(!result.get.isThrow)
      assert(result.get.get.status == Status.Forbidden)

      assertThrows[Service.PermissionDeniedException] {
        result.get.get.value
      }
    }

    it("should returns 400 when POST /auth with malformed Credential") {
      implicit val mockedAuthService: AuthService = stub[AuthService]
      val authController = new AuthController

      case class InvalidCredential(emmail: String, password: String)
      val credential = InvalidCredential("daniel@mail.com", "1234")

      (mockedAuthService.login _).verify(*).never()

      val input = Input.post("/auth").withBody(credential)

      val result = authController.getEndpoints(input).awaitOutput()

      assert(result.isDefined)
      assert(!result.get.isThrow)
      assert(result.get.get.status == Status.BadRequest)

      assertThrows[Exception] {
        result.get.get.value
      }
    }

    it("should returns 400 when POST /auth without a Credential") {
      implicit val mockedAuthService: AuthService = stub[AuthService]
      val authController = new AuthController

      (mockedAuthService.login _).verify(*).never()

      val input = Input.post("/auth")

      val result = authController.getEndpoints(input).awaitOutput()

      assert(result.isDefined)
      assert(!result.get.isThrow)
      assert(result.get.get.status == Status.BadRequest)

      assertThrows[Exception] {
        result.get.get.value
      }
    }
  }
}
