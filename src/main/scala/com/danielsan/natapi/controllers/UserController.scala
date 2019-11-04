package com.danielsan.natapi.controllers

import com.danielsan.natapi.endpoints.Authenticated._
import com.danielsan.natapi.resources.UserResource
import com.danielsan.natapi.resources.AuthResource.Payload
import com.danielsan.natapi.services.{Service, UserService}
import io.finch._
import shapeless.{:+:, CNil}

class UserController(service: UserService) {

  private def getUserByIdGeneric(payload: Payload, id: Long) = {
    service.getById(id)(payload) map {
      case Left(user) => Ok(user)
      case Right(ex) =>
        ex match {
          case e: Service.NotFoundException         => NotFound(new Exception("User not found!"))
          case e: Service.PermissionDeniedException => Unauthorized(new Exception("permission denied!"))
          case e: Service.Exception                 => InternalServerError(new Exception("Internal Server Error, Oops!"))
        }
    }
  }

  private val getUserById: Endpoint[UserResource.Private] = get(authenticated :: "user" :: path[Long]) { (payload: Payload, id: Long) =>
    getUserByIdGeneric(payload, id)
  }

  private val getUser: Endpoint[UserResource.Private] = get(authenticated :: "user") { payload: Payload =>
    getUserByIdGeneric(payload, payload.id)
  }

  def getEndpoints: Endpoint[UserResource.Private :+: UserResource.Private :+: CNil] = (getUserById :+: getUser).handle {
    case e: Exception => InternalServerError(e)
  }
}
