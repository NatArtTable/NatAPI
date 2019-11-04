package com.danielsan.natapi.controllers

import io.finch.circe._
import io.circe.generic.auto._
import io.finch._
import shapeless.{:+:, CNil}

import com.danielsan.natapi.resources.{CreatedResource, ImageResource, SearchResource}
import com.danielsan.natapi.services.{ImageService, Service}
import com.danielsan.natapi.endpoints.Authenticated._
import com.danielsan.natapi.resources.AuthResource.Payload

class ImageController(service: ImageService) {
  private val acceptedImage: Endpoint[ImageResource.Create] = jsonBody[ImageResource.Create]

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

  private val createImage: Endpoint[CreatedResource] = post(authenticated :: "image" :: acceptedImage) { (payload: Payload, image: ImageResource.Create) =>
    service.createImage(image)(payload) map {
      case Left(created) => Ok(created)
      case Right(ex)     => InternalServerError(new Exception("Oops!"))
    }
  }

  def getEndpoints: Endpoint[ImageResource.Full :+: SearchResource[ImageResource.Small] :+: CreatedResource :+: CNil] = (getImage :+: getImages :+: createImage).handle {
    case e: Exception => InternalServerError(e)
  }
}
