package com.danielsan.natapi.controllers

import com.danielsan.natapi.resources.AuthResource
import com.danielsan.natapi.services.{AuthService, Service}
import io.finch.{Endpoint, Forbidden, InternalServerError, NotFound, Ok, post}

class AuthController(service: AuthService) {

  private val auth: Endpoint[AuthResource.Token] = post("auth" :: AuthResource.acceptedCredential) { c: AuthResource.Credential =>
    {
      service.login(c) map {
        case Left(token) => Ok(token)
        case Right(ex) =>
          ex match {
            case e: Service.NotFoundException         => NotFound(new Exception("User not found!"))
            case e: Service.PermissionDeniedException => Forbidden(new Exception("Forbidden! Check your credentials"))
            case e: Service.Exception                 => InternalServerError(new Exception("Oops! Internal Server Error"))
          }
      }
    }
  }

  def getEndpoints: Endpoint[AuthResource.Token] = auth.handle {
    case e: Exception => InternalServerError(e)
  }
}
