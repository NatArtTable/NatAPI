package com.danielsan.natapi.controllers

import com.danielsan.natapi.resources.{ImageResource, SearchResource}
import com.danielsan.natapi.services.{ImageService, Service}
import com.danielsan.natapi.endpoints.Authenticated._
import com.danielsan.natapi.resources.AuthResource.Payload
import io.finch._
import shapeless.{:+:, CNil}

class ImageController(service: ImageService) {
  private val getImage: Endpoint[ImageResource.Full] = get(authenticated :: "image" :: path[Long]) { (payload: Payload, id: Long) =>
    service.getById(id)(payload) map {
      case Left(image) => Ok(image)
      case Right(ex) =>
        ex match {
          case e: Service.NotFoundException         => NotFound(new Exception("Image not found!"))
          case e: Service.PermissionDeniedException => Unauthorized(new Exception("unathourized!"))
          case e: Service.Exception                 => InternalServerError(new Exception("Oops! Internal Server Error"))
        }
    }
  }

  private val getImages: Endpoint[SearchResource[ImageResource.Small]] = get(authenticated :: "images") { payload: Payload =>
    service.getAll()(payload) map {
      case Left(images) => Ok(SearchResource(images))
      case Right(ex) =>
        ex match {
          case e: Service.Exception => InternalServerError(new Exception("Oops! Internal Server Error"))
        }
    }
  }

  def getEndpoints: Endpoint[ImageResource.Full :+: SearchResource[ImageResource.Small] :+: CNil] = (getImage :+: getImages).handle {
    case e: Exception => InternalServerError(e)
  }
}
