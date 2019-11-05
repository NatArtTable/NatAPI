package com.danielsan.natapi.services

import com.danielsan.natapi.repositories.UserRepository
import org.scalamock.scalatest.MockFactory
import org.scalatest.FunSpec

class AuthServiceImplSpec extends FunSpec with MockFactory {
  describe("test method login") {
    it("Should properly login with correct credentials") {
      val mockedRepository = mock[UserRepository]

      val service = new UserServiceImpl()(mockedRepository)
    }
  }
}
