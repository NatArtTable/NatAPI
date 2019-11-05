package com.danielsan.natapi.controllers

import java.io.FileOutputStream

import com.twitter.io.Buf
import java.nio.file.{Files, Path}
import java.nio.file.StandardCopyOption._

import com.twitter.finagle.http.exp.Multipart.{FileUpload, InMemoryFileUpload, OnDiskFileUpload}
import io.finch.{BadRequest, Endpoint, Forbidden, InternalServerError, NotFound}
import com.danielsan.natapi.services.Service
import com.danielsan.natapi.helpers
import com.danielsan.natapi.helpers.FileHandler.FileType
import com.danielsan.natapi.helpers.FutureConverters

object Controller {
  class Exception(msg: String) extends scala.Exception(msg)
  class InvalidParametersException(msg: String) extends Exception(msg)
  class MissingParameterException(msg: String) extends InvalidParametersException(msg: String)

  class FileHandler(file: FileUpload) extends helpers.FileHandler {
    override def fileType: FileType = file.contentType match {
      case "image/png"  => helpers.FileHandler.PNG
      case "image/jpeg" => helpers.FileHandler.JPEG
      case "image/txt"  => helpers.FileHandler.TXT
      case _            => throw new InvalidParametersException("Unsupported ContentType!")
    }

    override def saveToDisk(path: Path): Unit = {
      file match {
        case file: OnDiskFileUpload => Files.copy(file.content.toPath, path, REPLACE_EXISTING)
        case data: InMemoryFileUpload =>
          val bytes = Buf.ByteArray.Owned.extract(data.content)
          val fos = new FileOutputStream(path.toFile)
          fos.write(bytes)
      }
    }
  }
}

trait Controller[A] extends FutureConverters {
  protected def endpoints: Endpoint[A]

  def getEndpoints: Endpoint[A] = endpoints.handle {
    case e: io.finch.Error                        => BadRequest(e)
    case e: Controller.InvalidParametersException => BadRequest(e)
    case e: Service.NotFoundException             => NotFound(e)
    case e: Service.PermissionDeniedException     => Forbidden(e)
    case e: Service.InvalidParametersException    => BadRequest(e)
    case e: Service.Exception                     => InternalServerError(e)
    case _                                        => InternalServerError(new Exception("Oops!"))
  }
}
