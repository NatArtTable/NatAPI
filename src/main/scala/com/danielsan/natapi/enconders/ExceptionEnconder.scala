package com.danielsan.natapi.enconders

import com.danielsan.natapi.controllers.Controller
import com.danielsan.natapi.services.Service
import io.circe.{Encoder, Json}

trait ExceptionEnconder {
  protected implicit val exceptionEnconder: Encoder[Exception] = Encoder.instance {
    case e: io.finch.Error       => Json.obj("message" -> Json.fromString(e.getMessage), "status" -> Json.fromString("failed 10"))
    case e: Controller.Exception => Json.obj("message" -> Json.fromString(e.getMessage), "status" -> Json.fromString("failed 10"))
    case e: Service.Exception    => Json.obj("message" -> Json.fromString(e.getMessage), "status" -> Json.fromString("failed 20"))
    case _: Exception            => Json.obj("message" -> Json.fromString("Something went terrible wrong."), "status" -> Json.fromString("failed 30"))
  }
}
