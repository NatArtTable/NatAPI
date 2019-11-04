package com.danielsan.natapi.endpoints

import com.danielsan.natapi.helpers.{Crypto, PayloadSerializer}
import com.danielsan.natapi.resources.AuthResource.Payload
import io.finch.{Endpoint, Forbidden, Ok, Unauthorized, headerOption}

object Authenticated {
  private class MissingPayloadException(msg: String) extends Exception(msg)
  private class InvalidPayloadException(msg: String) extends Exception(msg)

  private def auth(h: String): Either[Payload, Exception] = {
    if (h == null) Right(new MissingPayloadException("Missing payload!"))
    else {
      try {
        val decrypted = Crypto.decrypt(h)
        Left(PayloadSerializer.deserialize(decrypted))
      } catch {
        case e: Exception => Right(new InvalidPayloadException(e.getMessage))
      }
    }
  }

  val authenticated: Endpoint[Payload] = headerOption("Authorization") mapOutput {
    case None => Unauthorized(new Exception("Missing authorization header!"))
    case Some(value) =>
      auth(value) match {
        case Left(payload) => Ok(payload)
        case Right(e)      => Forbidden(e)
      }
  }
}
