package com.danielsan.natapi.controllers

import com.danielsan.natapi.resources.AuthResource
import com.danielsan.natapi.services.AuthService
import io.finch.{Endpoint, Forbidden, NotFound, Ok, post, InternalServerError}

class AuthController(service: AuthService) {

  private val auth: Endpoint[AuthResource.Token] = post("auth" :: AuthResource.acceptedCredential) { c: AuthResource.Credential =>
    {
      service.login(c) map {
        case Left(token) => Ok(token)
        case Right(ex) =>
          ex match {
            case e: AuthService.UserNotFoundException  => NotFound(e)
            case e: AuthService.WrongPasswordException => Forbidden(e)
          }
      }
    }
  }

  def getEndpoints: Endpoint[AuthResource.Token] = auth.handle {
    case e: Exception => InternalServerError(e)
  }
}
