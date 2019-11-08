package com.danielsan.natapi.helpers

import java.io.FileOutputStream
import java.nio.file.{Path, Paths}
import java.util.UUID

import com.danielsan.natapi.controllers.Controller
import com.danielsan.natapi.helpers.FileHandler.FileType
import com.twitter.finagle.http.exp.Multipart.{FileUpload, InMemoryFileUpload, OnDiskFileUpload}
import com.twitter.io.Buf
import com.typesafe.config.{Config, ConfigFactory}

trait FileHandlerFinagle {
  private implicit val conf: Config = ConfigFactory.load()

  private val tmpFolder = conf.getString("file_handler.tmp_folder")

  implicit class FinagleFileHandler(file: FileUpload) extends FileHandler {
    override def fileType: FileType = file.contentType match {
      case "image/png"  => FileHandler.PNG
      case "image/jpeg" => FileHandler.JPEG
      case "image/txt"  => FileHandler.TXT
      case _            => throw new Controller.InvalidParametersException("Unsupported ContentType!")
    }

    override def getFilePath: Path = {
      file match {
        case file: OnDiskFileUpload => file.content.toPath
        case data: InMemoryFileUpload =>
          val bytes = Buf.ByteArray.Owned.extract(data.content)
          val tmpPath = Paths.get(tmpFolder, UUID.randomUUID().toString)
          val fos = new FileOutputStream(tmpPath.toFile)
          fos.write(bytes)

          tmpPath
      }
    }
  }
}
