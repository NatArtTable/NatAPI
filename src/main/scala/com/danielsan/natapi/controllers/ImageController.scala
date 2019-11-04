package com.danielsan.natapi.controllers

import com.danielsan.natapi.repositories.ImageRepository
import com.danielsan.natapi.resources.ImageResource
import io.finch._
import shapeless.{:+:, CNil}

class ImageController(repository: ImageRepository) {
  private val getImage: Endpoint[ImageResource] = get("image" :: path[Long]) { id: Long =>
    repository.getById(id) map {
      case Some(image) => Ok(ImageResource(image))
      case None        => NotFound(new Exception("Image not found!"))
    }
  }

  private val getImages: Endpoint[List[ImageResource]] = get("images") {
    repository.getAll() map { users =>
      Ok(users.toList.map(ImageResource(_)))
    }
  }

  def getEndpoints: Endpoint[ImageResource :+: List[ImageResource] :+: CNil] = (getImage :+: getImages).handle {
    case e: Exception => InternalServerError(e)
  }
}
