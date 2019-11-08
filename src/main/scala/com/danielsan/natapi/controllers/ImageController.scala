package com.danielsan.natapi.controllers

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

class ImageController(implicit service: ImageService, implicit val authentication: Authentication)
    extends Controller[ImageResources.Full :+: SearchResource[ImageResources.Small] :+: ImageResources.Created :+: CNil] {
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

  private val uploadImage: Endpoint[ImageResources.Created] = post(
    authentication.authenticated ::
      "image" :: "upload" ::
      fileUploadOption("image") ::
      paramOption("description") ::
      paramOption("tags")
  ) { (payload: Payload, image: Option[FileUpload], description: Option[String], tags: Option[String]) =>
    log.debug(s"Image upload route has been called by a user with id ${payload.id}.")

    image match {
      case Some(file) => {
        log.debug("Image is being uploaded.")
        val parsedTags = tags match {
          case Some(v) => Some(v.split(",").toSeq)
          case None    => None
        }

        log.debug("Trying to persist image via ImageService")
        val result = service.create(ImageResources.Create(file, description, parsedTags))(payload) map {
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

  override protected def endpoints: Endpoint[ImageResources.Full :+: SearchResource[ImageResources.Small] :+: ImageResources.Created :+: CNil] = getImage :+: getImages :+: uploadImage
}
