package com.danielsan.natapi.controllers

import io.finch.{BadRequest, Endpoint, Forbidden, InternalServerError, NotFound}
import com.danielsan.natapi.services.Service
import com.danielsan.natapi.helpers.{FileHandlerFinagleFileUpload, FutureConverters}

object Controller {
  class Exception(msg: String) extends scala.Exception(msg)
  class InvalidParametersException(msg: String) extends Exception(msg)
  class MissingParameterException(msg: String) extends InvalidParametersException(msg: String)
}

trait Controller[A] extends FutureConverters with FileHandlerFinagleFileUpload {
  protected def endpoints: Endpoint[A]

  def getEndpoints: Endpoint[A] = endpoints.handle {
    case e: io.finch.Error                        => BadRequest(e)
    case e: Controller.InvalidParametersException => BadRequest(e)
    case e: Service.NotFoundException             => NotFound(e)
    case e: Service.PermissionDeniedException     => Forbidden(e)
    case e: Service.InvalidParametersException    => BadRequest(e)
    case e: Service.Exception                     => InternalServerError(e)
    case _                                        => InternalServerError(new Exception("Oops!"))
  }
}
