package com.danielsan.natapi.controllers

import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

import io.finch._

import com.danielsan.natapi.endpoints.Authentication
import com.danielsan.natapi.resources.UserResource
import com.danielsan.natapi.resources.AuthResource.Payload
import com.danielsan.natapi.services.UserService

class UserController(implicit val service: UserService, implicit val authentication: Authentication) extends Controller {

  private def getUserByIdGeneric(payload: Payload, id: Long) = {
    service.getById(id)(payload) map {
      case Left(user) => Ok(user)
      case Right(ex)  => exceptionToResponse(ex)
    }
  }

  private val getUserById: Endpoint[UserResource.Generic] = get(authentication.authenticated :: "user" :: path[Long]) { (payload: Payload, id: Long) =>
    val result = getUserByIdGeneric(payload, id)
    result.asTwitter
  }

  private val getUser: Endpoint[UserResource.Generic] = get(authentication.authenticated :: "user") { payload: Payload =>
    val result = getUserByIdGeneric(payload, payload.id)
    result.asTwitter
  }

  def getEndpoints = (getUserById :+: getUser).handle {
    case e: Exception => InternalServerError(e)
  }
}
