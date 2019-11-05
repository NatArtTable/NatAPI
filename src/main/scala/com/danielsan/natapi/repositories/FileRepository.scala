package com.danielsan.natapi.repositories

import java.nio.file.Paths
import java.util.UUID

import com.danielsan.natapi.helpers.FileHandler

import scala.concurrent.{Future, ExecutionContext}
import ExecutionContext.Implicits.global

trait FileRepository extends Repository {
  implicit class URI(uri: String) {
    override def toString = uri
  }

  def save(file: FileHandler): URI
}

class FileRepositoryImpl(rootFolder: String) extends FileRepository {

  override def prepare(): Future[Unit] = {
    Future { Paths.get(rootFolder).toFile.mkdirs() }
  }

  override def save(file: FileHandler): URI = {
    val uuid = UUID.randomUUID().toString

    val path = Paths.get(rootFolder, s"$uuid.${file.fileType.extension}")
    file.saveToDisk(path)

    path.toUri.toString
  }
}
