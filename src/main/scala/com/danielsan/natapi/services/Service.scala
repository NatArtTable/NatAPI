package com.danielsan.natapi.services

object Service {
  abstract class Exception(msg: String) extends scala.Exception(msg)
  class PermissionDeniedException(msg: String) extends Exception(msg)
  class NotFoundException(msg: String) extends Exception(msg)
}
