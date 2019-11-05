package com.danielsan.natapi.controllers

import com.danielsan.natapi.endpoints.Authentication
import com.danielsan.natapi.resources.UserResource
import com.danielsan.natapi.resources.AuthResource.Payload
import com.danielsan.natapi.services.UserService
import io.finch._

class UserController(implicit val service: UserService, implicit val authentication: Authentication) extends Controller {

  private def getUserByIdGeneric(payload: Payload, id: Long) = {
    service.getById(id)(payload) map {
      case Left(user) => Ok(user)
      case Right(ex)  => exceptionToResponse(ex)
    }
  }

  private val getUserById: Endpoint[UserResource.Generic] = get(authentication.authenticated :: "user" :: path[Long]) { (payload: Payload, id: Long) =>
    getUserByIdGeneric(payload, id)
  }

  private val getUser: Endpoint[UserResource.Generic] = get(authentication.authenticated :: "user") { payload: Payload =>
    getUserByIdGeneric(payload, payload.id)
  }

  def getEndpoints = (getUserById :+: getUser).handle {
    case e: Exception => InternalServerError(e)
  }
}
