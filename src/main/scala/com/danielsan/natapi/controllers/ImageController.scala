package com.danielsan.natapi.controllers

import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import io.finch._
import com.danielsan.natapi.resources.{CreatedResource, ImageResource, SearchResource}
import com.danielsan.natapi.services.ImageService
import com.danielsan.natapi.endpoints.Authentication
import com.danielsan.natapi.resources.AuthResource.Payload
import com.twitter.finagle.http.exp.Multipart.FileUpload
import com.twitter.util.Future

class ImageController(implicit val service: ImageService, implicit val authentication: Authentication) extends Controller {

  private val getImage: Endpoint[ImageResource.Full] = get(authentication.authenticated :: "image" :: path[Long]) { (payload: Payload, id: Long) =>
    val result = service.getById(id)(payload) map ({
      case Left(image) => Ok(image)
      case Right(ex)   => exceptionToResponse(ex)
    })

    result.asTwitter
  }

  private val getImages: Endpoint[SearchResource[ImageResource.Small]] = get(authentication.authenticated :: "images") { payload: Payload =>
    val result = service.getAll()(payload) map {
      case Left(images) => Ok(SearchResource(images))
      case Right(ex)    => exceptionToResponse(ex)
    }

    result.asTwitter
  }

  private val uploadImage: Endpoint[CreatedResource] = post(
    authentication.authenticated ::
      "image" :: "upload" ::
      fileUploadOption("image") ::
      paramOption("description") ::
      paramOption("tags")
  ) { (payload: Payload, image: Option[FileUpload], description: Option[String], tags: Option[String]) =>
    image match {
      case Some(file) => {
        val parsedTags = tags match {
          case Some(v) => Some(v.split(",").toSeq)
          case None    => None
        }

        val result = service.createImage(ImageResource.Create(new Controller.FileHandler(file), description, parsedTags))(payload) map {
          case Left(created) => Ok(created)
          case Right(ex)     => exceptionToResponse(ex)
        }

        result.asTwitter
      }
      case None => Future { exceptionToResponse(new Controller.MissingParameterException("Missing image file! Send it using MultiPart Form")) }
    }
  }

  def getEndpoints = (getImage :+: getImages :+: uploadImage).handle {
    case e: Exception => InternalServerError(e)
  }
}
