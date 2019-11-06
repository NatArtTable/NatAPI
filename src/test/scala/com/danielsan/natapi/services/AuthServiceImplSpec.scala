package com.danielsan.natapi.services

import com.danielsan.natapi.models.User
import com.danielsan.natapi.resources.AuthResource.{Credential, Payload}

import scala.concurrent.Await
import scala.concurrent.duration._

class AuthServiceImplSpec extends BaseSpec {
  var daniel: User = _
  var jujuba: User = _

  before {
    server.prepare()

    daniel = server.addUser(User(-1, "daniel", "daniel@mail.com", "1234"))
    jujuba = server.addUser(User(-1, "jujuba", "jujuba@mail.com", "4321"))
  }

  describe("Testing login service") {
    it("Should return a token if user login with correct credentials") {
      val result = Await.result(server.authService.login(Credential(daniel)), 3.seconds)
      assert(result.isLeft)
    }

    it("Should return a NotFoundException if trying to login with non existing email") {
      val result = Await.result(server.authService.login(Credential("wrong@mail.com", "super_secure_password")), 3.seconds)
      assert(result.isRight)
      assert(result.right.get.isInstanceOf[Service.NotFoundException])
    }

    it("Should return a PermissionDeniedException if trying to login with a wrong password") {
      val result = Await.result(server.authService.login(Credential("daniel@mail.com", "123typo4")), 3.seconds)
      assert(result.isRight)
      assert(result.right.get.isInstanceOf[Service.PermissionDeniedException])
    }
  }

  describe("Testing payload validation service (auth method)") {
    it("Should correctly decode a valid payload generated by the login service") {
      val Left(token) = Await.result(server.authService.login(Credential(daniel)), 3.seconds)

      val result = server.authService.auth(token.token)

      assert(result.isLeft)
      assert(result.left.get == Payload(daniel))
    }

    it("Should return a PermissionDeniedException trying to decode a invalid token") {
      val result = server.authService.auth("white noise")

      assert(result.isRight)
      assert(result.getOrElse(null).isInstanceOf[Service.PermissionDeniedException])
    }

    it("Should return a MissingParameterException trying to decode a null token") {
      val result = server.authService.auth(null)

      assert(result.isRight)
      assert(result.getOrElse(null).isInstanceOf[Service.MissingParameterException])
    }
  }
}
