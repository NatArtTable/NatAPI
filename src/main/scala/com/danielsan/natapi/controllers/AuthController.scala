package com.danielsan.natapi.controllers

import io.finch.{Endpoint, Forbidden, InternalServerError, NotFound, Ok, jsonBody, post}
import io.finch.circe._
import io.circe.generic.auto._

import com.danielsan.natapi.resources.AuthResource
import com.danielsan.natapi.resources.AuthResource.Credential
import com.danielsan.natapi.services.{AuthService, Service}

class AuthController(service: AuthService) {
  private val acceptedCredential: Endpoint[Credential] = jsonBody[Credential]

  private val auth: Endpoint[AuthResource.Token] = post("auth" :: acceptedCredential) { c: AuthResource.Credential =>
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
