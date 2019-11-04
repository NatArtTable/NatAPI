package com.danielsan.natapi.controllers

import com.danielsan.natapi.services.Service
import io.finch.{Forbidden, InternalServerError, NotFound, BadRequest, Output}

trait Controller {
  protected def exceptionToResponse(e: Exception): Output[Nothing] = {
    e match {
      case e: Service.NotFoundException          => NotFound(e)
      case e: Service.PermissionDeniedException  => Forbidden(e)
      case e: Service.InvalidParametersException => BadRequest(e)
      case e: Service.Exception                  => InternalServerError(e)
      case _                                     => InternalServerError(new Exception("Oops!"))

    }
  }
}
