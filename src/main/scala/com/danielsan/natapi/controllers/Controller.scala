package com.danielsan.natapi.controllers

import io.finch.{BadRequest, Endpoint, Forbidden, InternalServerError, NotFound}
import com.danielsan.natapi.services.Service
import com.danielsan.natapi.helpers.{FileHandlerFinagleFileUpload, FutureConverters}
import org.slf4j.LoggerFactory

object Controller {
  sealed class Exception(msg: String) extends scala.Exception(msg)
  class InvalidParametersException(msg: String) extends Exception(msg)
  class MissingParameterException(msg: String) extends InvalidParametersException(msg)
  class FileNotFoundException(msg: String) extends Exception(msg)
}

trait Controller[A] extends FutureConverters with FileHandlerFinagleFileUpload {
  private val log = LoggerFactory.getLogger(this.getClass)

  protected def endpoints: Endpoint[A]

  def getEndpoints: Endpoint[A] = endpoints.handle {
    case e: io.finch.Error                        => BadRequest(e)
    case e: Controller.InvalidParametersException => BadRequest(e)
    case e: Controller.FileNotFoundException      => NotFound(e)
    case e: Service.NotFoundException             => NotFound(e)
    case e: Service.PermissionDeniedException     => Forbidden(e)
    case e: Service.InvalidParametersException    => BadRequest(e)
    case e: Service.Exception                     => InternalServerError(e)
    case e: Exception =>
      log.error(s"Internal Server Error! message: ${e.getMessage}")
      InternalServerError(new Exception("Oops!"))
  }
}
