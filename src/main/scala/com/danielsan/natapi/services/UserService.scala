package com.danielsan.natapi.services

import com.danielsan.natapi.resources.UserResource
import com.danielsan.natapi.repositories.UserRepository
import com.danielsan.natapi.resources.AuthResource.Payload
import com.danielsan.natapi.services
import com.twitter.util.Future

object UserService {
  sealed abstract class Exception(msg: String) extends scala.Exception(msg)
  class PermissionDeniedException(msg: String) extends Exception(msg)
  class NotFoundException(msg: String) extends Exception(msg)
}

class UserService(repository: UserRepository) {
  def getById(id: Long)(implicit payload: Payload): Future[Either[UserResource.Private, Exception]] = {
    if (payload.id != id) {
      Future { Right(new UserService.PermissionDeniedException("You cannot access the private information of other users!")) }
    } else {
      repository.getById(id) map {
        case Some(user) => Left(UserResource.Private(user))
        case None       => Right(new services.UserService.NotFoundException("User not found!"))
      }
    }
  }
}
