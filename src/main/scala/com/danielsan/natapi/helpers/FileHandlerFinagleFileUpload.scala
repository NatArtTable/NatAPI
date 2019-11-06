package com.danielsan.natapi.helpers

import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

import java.io.FileOutputStream
import java.nio.file.{Files, Path}
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

import com.danielsan.natapi.controllers.Controller.InvalidParametersException
import com.danielsan.natapi.helpers
import com.danielsan.natapi.helpers.FileHandler.FileType
import com.twitter.finagle.http.exp.Multipart.{FileUpload, InMemoryFileUpload, OnDiskFileUpload}
import com.twitter.io.Buf

import scala.concurrent.Future

trait FileHandlerFinagleFileUpload {
  implicit class FileUploadWithFileHandler(file: FileUpload) extends helpers.FileHandler {
    override def fileType: FileType = file.contentType match {
      case "image/png"  => helpers.FileHandler.PNG
      case "image/jpeg" => helpers.FileHandler.JPEG
      case "image/txt"  => helpers.FileHandler.TXT
      case _            => throw new InvalidParametersException("Unsupported ContentType!")
    }

    override def saveToDisk(path: Path): Future[Unit] = {
      Future {
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
}
