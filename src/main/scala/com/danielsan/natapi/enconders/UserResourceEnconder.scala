package com.danielsan.natapi.enconders

import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.Encoder

import com.danielsan.natapi.resources.UserResource

trait UserResourceEnconder {
  implicit protected val userResourceEnconder: Encoder[UserResource.Generic] = Encoder.instance {
    case user: UserResource.Private => user.asJson
    case user: UserResource.Public  => user.asJson
  }
}
