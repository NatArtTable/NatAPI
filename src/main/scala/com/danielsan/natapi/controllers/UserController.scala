package com.danielsan.natapi.controllers

import com.danielsan.natapi.endpoints.Authenticated._
import com.danielsan.natapi.resources.UserResource
import com.danielsan.natapi.resources.AuthResource.Payload
import com.danielsan.natapi.services.{Service, UserService}
import io.finch._
import shapeless.{:+:, CNil}

class UserController(service: UserService) {
  private val getUserById: Endpoint[UserResource.Private] = get(authenticated :: "user" :: path[Long]) { (payload: Payload, id: Long) =>
    service.getById(id)(payload) map {
      case Left(user) => Ok(user)
      case Right(ex) =>
        ex match {
          case ex: Service.NotFoundException         => NotFound(ex)
          case ex: Service.PermissionDeniedException => Unauthorized(ex)
        }
    }
  }

  private val getUser: Endpoint[UserResource.Private] = get(authenticated :: "user") { payload: Payload =>
    service.getById(payload.id)(payload) map {
      case Left(user) => Ok(user)
      case Right(ex) =>
        ex match {
          case ex: Service.NotFoundException         => NotFound(ex)
          case ex: Service.PermissionDeniedException => Unauthorized(ex)
        }
    }
  }

  def getEndpoints: Endpoint[UserResource.Private :+: UserResource.Private :+: CNil] = (getUserById :+: getUser).handle {
    case e: Exception => InternalServerError(e)
  }
}
