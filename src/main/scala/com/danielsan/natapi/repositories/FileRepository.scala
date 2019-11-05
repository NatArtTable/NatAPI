package com.danielsan.natapi.repositories

import java.nio.file.Paths
import java.util.UUID

import com.danielsan.natapi.helpers.FileHandler
import com.twitter.util.Future

trait FileRepository {
  implicit class URI(uri: String) {
    override def toString = uri
  }

  def save(file: FileHandler): URI
  def prepare(): Future[Boolean]
}

class FileRepositoryImpl(rootFolder: String) extends FileRepository {

  override def prepare(): Future[Boolean] = {
    Future { Paths.get(rootFolder).toFile.mkdirs() }
  }

  override def save(file: FileHandler): URI = {
    val uuid = UUID.randomUUID().toString

    val path = Paths.get(rootFolder, s"$uuid.${file.fileType.extension}")
    file.saveToDisk(path)

    path.toUri.toString
  }
}
