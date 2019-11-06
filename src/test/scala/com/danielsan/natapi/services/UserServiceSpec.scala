package com.danielsan.natapi.services

import com.danielsan.natapi.models.User
import com.danielsan.natapi.resources.AuthResource.Payload
import com.danielsan.natapi.resources.UserResource

import scala.concurrent.duration._
import scala.concurrent.Await

class UserServiceSpec extends BaseSpec {
  var daniel: User = _
  var jujuba: User = _

  before {
    server.prepare()

    daniel = server.addUser(User(-1, "daniel", "daniel@mail.com", "1234"))
    jujuba = server.addUser(User(-1, "jujuba", "jujuba@mail.com", "4321"))
  }

  describe("test getById service") {
    it("should return the correct Public information of the user if another user make a valid request for") {
      val result = Await.result(server.userService.getById(jujuba.id)(Payload(daniel)), 2.seconds)

      assert(result.isLeft)
      assert(result.left.getOrElse(null) == UserResource.Public(jujuba))
    }

    it("should return the correct Private information of the user if a user requests for it own information") {
      val result = Await.result(server.userService.getById(daniel.id)(Payload(daniel)), 2.seconds)

      assert(result.isLeft)
      assert(result.left.getOrElse(null) == UserResource.Private(daniel))
    }

    it("should return a NotFound Exception if a user requests for a non existing Id") {
      val result = Await.result(server.userService.getById(32)(Payload(daniel)), 2.seconds)

      assert(result.isRight)
      assert(result.getOrElse(null).isInstanceOf[Service.NotFoundException])
    }
  }
}
