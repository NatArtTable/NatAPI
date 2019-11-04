package com.danielsan.natapi.services

import java.util.Base64

import com.danielsan.natapi.models.Image
import com.danielsan.natapi.repositories.ImageRepository
import com.danielsan.natapi.resources.AuthResource.Payload
import com.danielsan.natapi.resources.{CreatedResource, ImageResource}
import com.twitter.util.Future

trait ImageService {
  def getById(id: Long)(implicit payload: Payload): Future[Either[ImageResource.Full, Service.Exception]]
  def getAll()(implicit payload: Payload): Future[Either[Seq[ImageResource.Small], Service.Exception]]
  def createImage(image: ImageResource.Create)(implicit payload: Payload): Future[Either[CreatedResource, Service.Exception]]
}

class ImageServiceImpl(repository: ImageRepository) extends ImageService {
  override def getById(id: Long)(implicit payload: Payload): Future[Either[ImageResource.Full, Service.Exception]] = {
    repository.getById(id) map {
      case Some(image) => {
        if (image.owner_id != payload.id) Right(new Service.PermissionDeniedException("You cannot access the image of other user!"))
        else Left(ImageResource.Full(image))
      }
      case None => Right(new Service.NotFoundException(s"Image with id $id not found!"))
    }
  }

  override def getAll()(implicit payload: Payload): Future[Either[Seq[ImageResource.Small], Service.Exception]] = {
    repository.filter("owner_id", payload.id) map { images =>
      Left(images.map(ImageResource.Small(_)))
    }
  }

  override def createImage(image: ImageResource.Create)(implicit payload: Payload): Future[Either[CreatedResource, Service.Exception]] = {
    val newImage = Image.New(image.description, image.tags, payload.id)
    repository.create(newImage) map { created =>
      Left(CreatedResource(created))
    }
  }
}
