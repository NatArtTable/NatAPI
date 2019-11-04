package com.danielsan.natapi.controllers

import io.finch.{Endpoint, InternalServerError, Ok, jsonBody, post}
import io.finch.circe._
import io.circe.generic.auto._

import com.danielsan.natapi.resources.AuthResource
import com.danielsan.natapi.resources.AuthResource.Credential
import com.danielsan.natapi.services.AuthService

class AuthController(implicit val service: AuthService) extends Controller {
  private val acceptedCredential: Endpoint[Credential] = jsonBody[Credential]

  private val auth: Endpoint[AuthResource.Token] = post("auth" :: acceptedCredential) { c: AuthResource.Credential =>
    {
      service.login(c) map {
        case Left(token) => Ok(token)
        case Right(ex)   => exceptionToResponse(ex)
      }
    }
  }

  def getEndpoints: Endpoint[AuthResource.Token] = auth.handle {
    case e: Exception => InternalServerError(e)
  }
}
