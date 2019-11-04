package com.danielsan.natapi.services

object Service {
  sealed abstract class Exception(msg: String) extends scala.Exception(msg)
  class InternalServerError(msg: String) extends Exception(msg)
  class PermissionDeniedException(msg: String) extends Exception(msg)
  class NotFoundException(msg: String) extends Exception(msg)
  class InvalidParametersException(msg: String) extends Exception(msg)
  class MissingParameterException(msg: String) extends InvalidParametersException(msg: String)
}
