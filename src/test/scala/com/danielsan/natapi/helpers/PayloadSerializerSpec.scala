package com.danielsan.natapi.helpers

import com.danielsan.natapi.resources.AuthResource.Payload
import org.scalatest.FunSpec

class PayloadSerializerSpec extends FunSpec {

  val random = scala.util.Random

  describe("PayloadSerializer testing") {
    it("should serialize and deserializer correctly a random payload") {
      val payload = Payload(random.nextLong(), random.nextString(150))

      val serialized = PayloadSerializer.serialize(payload)
      val deserialized = PayloadSerializer.deserialize(serialized)

      assert(deserialized == payload)
    }
  }
}
