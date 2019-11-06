package com.danielsan.natapi.helpers

import java.nio.file.Path

import scala.concurrent.Future

trait FileHandler {
  def fileType: FileHandler.FileType
  def saveToDisk(path: Path): Future[Unit]
}

object FileHandler {
  sealed abstract class FileType(ext: String) {
    val extension = ext
  }

  final case object JPEG extends FileType("jpeg")
  final case object PNG extends FileType("png")
  final case object TXT extends FileType("txt")
}
