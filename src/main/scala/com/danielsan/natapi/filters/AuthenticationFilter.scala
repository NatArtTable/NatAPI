package com.danielsan.natapi.filters

import com.danielsan.natapi.controllers
import com.danielsan.natapi.resources.AuthResource.Payload
import com.danielsan.natapi.services.AuthService
import io.finch.{Endpoint, Ok, headerOption}

trait Authentication {
  val authenticated: Endpoint[Payload]
}

class AuthenticationImpl(implicit val service: AuthService) extends Authentication {
  override val authenticated = headerOption("Authorization") mapOutput {
    case None => throw new controllers.Controller.MissingParameterException("Missing authorization header!")
    case Some(value) =>
      service.auth(value) match {
        case Left(payload) => Ok(payload)
        case Right(e)      => throw e
      }
  }
}
