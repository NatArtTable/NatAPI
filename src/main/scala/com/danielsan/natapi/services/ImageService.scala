package com.danielsan.natapi.services

import com.danielsan.natapi.helpers.FileHandler
import com.danielsan.natapi.models.ImageModels
import com.danielsan.natapi.repositories.ImageRepository
import com.danielsan.natapi.resources.AuthResource.Payload
import com.danielsan.natapi.resources.ImageResources

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global

trait ImageService {
  def getById(id: Long)(implicit payload: Payload): Future[Either[ImageResources.Full, Service.Exception]]
  def getAll()(implicit payload: Payload): Future[Either[Seq[ImageResources.Small], Service.Exception]]
  def create(image: ImageResources.Create)(implicit payload: Payload): Future[Either[ImageResources.Created, Service.Exception]]
  def deleteById(id: Long)(implicit payload: Payload): Future[Either[ImageResources.Deleted, Service.Exception]]
}

class ImageServiceImpl(implicit repository: ImageRepository) extends ImageService {
  override def getById(id: Long)(implicit payload: Payload): Future[Either[ImageResources.Full, Service.Exception]] = {
    repository.getById(id) map {
      case Some(image) => {
        if (image.owner_id != payload.id) Right(new Service.PermissionDeniedException("You cannot access the image of another user!"))
        else Left(ImageResources.Full(image))
      }
      case None => Right(new Service.NotFoundException(s"Image with id $id not found!"))
    }
  }

  override def getAll()(implicit payload: Payload): Future[Either[Seq[ImageResources.Small], Service.Exception]] = {
    repository.getAllByOwnerId(payload.id) map { images =>
      Left(images.map(ImageResources.Small(_)))
    }
  }

  override def create(image: ImageResources.Create)(implicit payload: Payload): Future[Either[ImageResources.Created, Service.Exception]] = {
    if (!Seq(FileHandler.JPEG, FileHandler.PNG).contains(image.file.fileType))
      return Future {
        Right(new Service.InvalidParametersException("image type not supported. Supported image types: jpg, png."))
      }

    val newImage = ImageModels.New(image.file, image.description.getOrElse(""), image.tags.getOrElse(Seq()), payload.id, image.width, image.height)
    repository.create(newImage) map { created =>
      Left(ImageResources.Created(created))
    }
  }

  override def deleteById(id: Long)(implicit payload: Payload): Future[Either[ImageResources.Deleted, Service.Exception]] = {
    repository.getById(id) map {
      case Some(image) =>
        if (image.owner_id != payload.id) Future.successful(Right(new Service.PermissionDeniedException("You cannot delete the image of another user!")))
        else repository.deleteById(id).map(Function.const(Left(ImageResources.Deleted(id))))
      case None => Future.successful(Right(new Service.NotFoundException(s"Image with id $id not found.")))
    } flatMap identity
  }
}
