package com.danielsan.natapi.helpers

import org.scalatest.FunSpec

class CryptoSpec extends FunSpec {

  describe("Crypto") {
    it("should encrypt and decrypt correctly 'Hello World!'") {
      val message = "Hello World!"
      val encrypted = Crypto.encrypt(message)
      val decrypted = Crypto.decrypt(encrypted)

      assert(decrypted == message)
    }

    it("should encrypt and decrypt correctly 'お前はもう死んでいる!'") {
      val message = "お前はもう死んでいる!"
      val encrypted = Crypto.encrypt(message)
      val decrypted = Crypto.decrypt(encrypted)

      assert(decrypted == message)
    }
  }
}
