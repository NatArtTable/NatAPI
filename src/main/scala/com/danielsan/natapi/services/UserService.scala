package com.danielsan.natapi.services

import com.danielsan.natapi.resources.UserResource
import com.danielsan.natapi.repositories.UserRepository
import com.danielsan.natapi.resources.AuthResource.Payload
import com.danielsan.natapi.services
import com.twitter.util.Future

trait UserService {
  def getById(id: Long)(implicit payload: Payload): Future[Either[UserResource.Private, Service.Exception]]
}

class UserServiceImpl(repository: UserRepository) extends UserService {
  def getById(id: Long)(implicit payload: Payload): Future[Either[UserResource.Private, Service.Exception]] = {
    if (payload.id != id) {
      Future { Right(new Service.PermissionDeniedException("You cannot access the private information of other users!")) }
    } else {
      repository.getById(id) map {
        case Some(user) => Left(UserResource.Private(user))
        case None       => Right(new services.Service.NotFoundException("User not found!"))
      }
    }
  }
}
