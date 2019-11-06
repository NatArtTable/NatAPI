package com.danielsan.natapi.helpers

import com.danielsan.natapi.resources.AuthResource.Payload

import scala.xml.XML

object PayloadSerializer {
  def serialize(payload: Payload): String = { <payload><id>{payload.id}</id><email>{payload.email}</email></payload> } toString ()

  def deserialize(s: String): Payload = {
    val xml = XML.loadString(s)
    val id = (xml \ "id").text.toLong
    val email = (xml \ "email").text

    Payload(id, email)
  }
}
