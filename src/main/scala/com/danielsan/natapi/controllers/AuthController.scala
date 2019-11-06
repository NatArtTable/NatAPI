package com.danielsan.natapi.controllers

import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

import io.finch.{Endpoint, InternalServerError, Ok, jsonBody, post}
import io.finch.circe._
import io.circe.generic.auto._

import com.danielsan.natapi.resources.AuthResource
import com.danielsan.natapi.resources.AuthResource.Credential
import com.danielsan.natapi.services.AuthService

class AuthController(implicit service: AuthService) extends Controller[AuthResource.Token] {
  private val acceptedCredential: Endpoint[Credential] = jsonBody[Credential]

  private val auth: Endpoint[AuthResource.Token] = post("auth" :: acceptedCredential) { c: AuthResource.Credential =>
    {
      val result = service.login(c) map {
        case Left(token) => Ok(token)
        case Right(ex)   => throw ex
      }

      result.asTwitter
    }
  }

  override protected def endpoints: Endpoint[AuthResource.Token] = auth
}
