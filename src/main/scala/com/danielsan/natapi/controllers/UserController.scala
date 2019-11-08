package com.danielsan.natapi.controllers

import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import io.finch._
import com.danielsan.natapi.filters.Authentication
import com.danielsan.natapi.resources.UserResource
import com.danielsan.natapi.resources.AuthResource.Payload
import com.danielsan.natapi.services.UserService
import org.slf4j.LoggerFactory
import shapeless.{:+:, CNil}

class UserController(implicit service: UserService, implicit val authentication: Authentication) extends Controller[UserResource.Generic :+: UserResource.Generic :+: CNil] {
  private val log = LoggerFactory.getLogger(this.getClass)

  private def getUserByIdGeneric(payload: Payload, id: Long) = {
    service.getById(id)(payload) map {
      case Left(user) => Ok(user)
      case Right(ex)  => throw ex
    }
  }

  private val getUserById: Endpoint[UserResource.Generic] = get(authentication.authenticated :: "user" :: path[Long]) { (payload: Payload, id: Long) =>
    val result = getUserByIdGeneric(payload, id)
    result.asTwitter
  }

  private val getUser: Endpoint[UserResource.Generic] = get(authentication.authenticated :: "user") { payload: Payload =>
    log.debug(s"GET /user route called by a user with id: ${payload.id}")
    val result = getUserByIdGeneric(payload, payload.id)
    result.asTwitter
  }

  override protected def endpoints: Endpoint[UserResource.Generic :+: UserResource.Generic :+: CNil] = getUserById :+: getUser
}
