package com.danielsan.natapi.controllers

import com.danielsan.natapi.resources.AuthResource.{Credential, Token}
import com.danielsan.natapi.services.{AuthService}
import io.finch.Input
import org.scalatest.FunSpec
import io.circe.generic.auto._
import io.finch.circe._
import org.scalamock.scalatest.MockFactory

import scala.concurrent.Future

class AuthControllerSpec extends FunSpec with MockFactory {

  describe("Testing authentication endpoint") {
    it("Should return the user Token when POST /auth with value Credentials") {
      implicit val mockedAuthService: AuthService = mock[AuthService]
      val authController = new AuthController

      val credential = Credential("daniel@mail.com", "1234")
      val token = Token("123")

      (mockedAuthService.login _).expects(Credential("daniel@mail.com", "1234")).returning(Future.successful(Left(token)))

      val input = Input.post("/auth").withBody(credential)

      val result = authController.getEndpoints(input).awaitValueUnsafe()

      assert(result.isDefined)
      assert(result.get == token)
    }
  }
}
