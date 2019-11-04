package com.danielsan.natapi.controllers

import io.finch.circe._
import io.circe.generic.auto._
import io.finch._
import shapeless.{:+:, CNil}

import com.danielsan.natapi.resources.{CreatedResource, ImageResource, SearchResource}
import com.danielsan.natapi.services.ImageService
import com.danielsan.natapi.endpoints.Authentication
import com.danielsan.natapi.resources.AuthResource.Payload

class ImageController(implicit val service: ImageService, implicit val authentication: Authentication) extends Controller {
  private val acceptedImage: Endpoint[ImageResource.Create] = jsonBody[ImageResource.Create]

  private val getImage: Endpoint[ImageResource.Full] = get(authentication.authenticated :: "image" :: path[Long]) { (payload: Payload, id: Long) =>
    service.getById(id)(payload) map {
      case Left(image) => Ok(image)
      case Right(ex)   => exceptionToResponse(ex)
    }
  }

  private val getImages: Endpoint[SearchResource[ImageResource.Small]] = get(authentication.authenticated :: "images") { payload: Payload =>
    service.getAll()(payload) map {
      case Left(images) => Ok(SearchResource(images))
      case Right(ex)    => exceptionToResponse(ex)
    }
  }

  private val createImage: Endpoint[CreatedResource] = post(authentication.authenticated :: "image" :: acceptedImage) { (payload: Payload, image: ImageResource.Create) =>
    service.createImage(image)(payload) map {
      case Left(created) => Ok(created)
      case Right(ex)     => exceptionToResponse(ex)
    }
  }

  def getEndpoints: Endpoint[ImageResource.Full :+: SearchResource[ImageResource.Small] :+: CreatedResource :+: CNil] = (getImage :+: getImages :+: createImage).handle {
    case e: Exception => InternalServerError(e)
  }
}
