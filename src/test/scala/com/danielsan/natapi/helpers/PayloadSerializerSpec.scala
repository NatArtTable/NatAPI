package com.danielsan.natapi.helpers

import com.danielsan.natapi.resources.AuthResource.Payload
import org.scalatest.FunSpec

class PayloadSerializerSpec extends FunSpec {

  describe("PayloadSerializer testing") {
    it("should serialize and deserializer correctly a random payload") {
      val payload = Payload(516624021790483476L, "녣竞캦㴩&馦낳触៧\u2062坘웢奁䛪ꁠડᭁ䠈⒌蓦䃕퐿ㅫ糀꙽ߒ䦪\u2BE6輅솓趇靹턞쨴㡮ᯡ렉舆ᣀ㒳墾匿⧤棱粋㥞᭱ꓣ蛯퉕〾쾩ȩ䐗鱓콈\u1CB0菏덴⋮黈굉洑㛕鵕꿍䘭ⶃ휙坙韫蜈船쯱暟篁瞱ᭆ䓏㕼숳蝣")

      val serialized = PayloadSerializer.serialize(payload)
      val deserialized = PayloadSerializer.deserialize(serialized)

      assert(deserialized == payload)
    }
  }
}
