package com.danielsan.natapi.controllers

import com.danielsan.natapi
import com.danielsan.natapi.controllers

import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import io.finch._
import com.danielsan.natapi.resources.{ImageResources, SearchResource}
import com.danielsan.natapi.services.ImageService
import com.danielsan.natapi.filters.Authentication
import com.danielsan.natapi.resources.AuthResource.Payload
import com.twitter.finagle.http.exp.Multipart.FileUpload
import org.slf4j.LoggerFactory
import shapeless.{:+:, CNil}

import scala.util.Try

class ImageController(implicit service: ImageService, implicit val authentication: Authentication)
    extends Controller[ImageResources.Full :+: SearchResource[ImageResources.Small] :+: ImageResources.Created :+: ImageResources.Deleted :+: CNil] {
  private val log = LoggerFactory.getLogger(this.getClass)

  private val getImage: Endpoint[ImageResources.Full] = get(authentication.authenticated :: "image" :: path[Long]) { (payload: Payload, id: Long) =>
    val result = service.getById(id)(payload) map {
      case Left(image) => Ok(image)
      case Right(ex)   => throw ex
    }

    result.asTwitter
  }

  private val getImages: Endpoint[SearchResource[ImageResources.Small]] = get(authentication.authenticated :: "images") { payload: Payload =>
    val result = service.getAll()(payload) map {
      case Left(images) => Ok(SearchResource(images))
      case Right(ex)    => throw ex
    }

    result.asTwitter
  }

  private val deleteImage: Endpoint[ImageResources.Deleted] = delete(authentication.authenticated :: "image" :: path[Long]) { (payload: Payload, id: Long) =>
    {
      val result = service.deleteById(id)(payload) map {
        case Left(response) => Ok(response)
        case Right(ex)      => throw ex
      }

      result.asTwitter
    }
  }

  private val uploadImage: Endpoint[ImageResources.Created] = post(
    authentication.authenticated ::
      "image" :: "upload" ::
      fileUploadOption("image") ::
      paramOption("description") ::
      paramOption("tags") ::
      paramOption("width") ::
      paramOption("height")
  ) { (payload: Payload, image: Option[FileUpload], description: Option[String], tags: Option[String], width: Option[String], height: Option[String]) =>
    log.debug(s"Image upload route has been called by a user with id ${payload.id}.")

    if (width.isEmpty) throw new Controller.MissingParameterException("Missing width!")
    if (height.isEmpty) throw new Controller.MissingParameterException("Missing height!")

    val widthInt = Try(width.get.toInt)
    val heightInt = Try(height.get.toInt)

    if (widthInt.isFailure) throw new Controller.InvalidParametersException("Width should be a integer!")
    if (heightInt.isFailure) throw new Controller.InvalidParametersException("Height should be a integer!")

    image match {
      case Some(file) => {
        log.debug("Image is being uploaded.")
        val parsedTags = tags match {
          case Some(v) => Some(v.split(",").toSeq)
          case None    => None
        }

        log.debug("Trying to persist image via ImageService")
        val result = service.create(ImageResources.Create(file, description, parsedTags, widthInt.get, heightInt.get))(payload) map {
          case Left(created) =>
            log.debug("Image persisted! Responding status Ok")
            Ok(created)
          case Right(ex) =>
            log.debug(s"Failed to persist Image =( Exception Message: ${ex.getMessage}")
            throw ex
        }

        result.asTwitter
      }
      case None =>
        log.debug("No image supplied, throwing a MissingParameterException.")
        throw new Controller.MissingParameterException("Missing image file! Send it using MultiPart Form")
    }
  }

  override protected def endpoints: Endpoint[ImageResources.Full :+: SearchResource[ImageResources.Small] :+: ImageResources.Created :+: ImageResources.Deleted :+: CNil] =
    getImage :+: getImages :+: uploadImage :+: deleteImage
}
