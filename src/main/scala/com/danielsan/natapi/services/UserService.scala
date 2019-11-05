package com.danielsan.natapi.services

import com.danielsan.natapi.resources.UserResource
import com.danielsan.natapi.repositories.UserRepository
import com.danielsan.natapi.resources.AuthResource.Payload
import com.danielsan.natapi.services

import scala.concurrent.{Future, ExecutionContext}
import ExecutionContext.Implicits.global

trait UserService {
  def getById(id: Long)(implicit payload: Payload): Future[Either[UserResource.Generic, Service.Exception]]
}

class UserServiceImpl(implicit val repository: UserRepository) extends UserService {
  def getById(id: Long)(implicit payload: Payload): Future[Either[UserResource.Generic, Service.Exception]] = {
    repository.getById(id) map {
      case Some(user) => Left(if (id == payload.id) UserResource.Private(user) else UserResource.Public(user))
      case None       => Right(new services.Service.NotFoundException("User not found!"))
    }
  }
}
