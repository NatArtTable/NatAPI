package com.danielsan.natapi.repositories

import java.nio.file.{Path, Paths}
import java.util.UUID

import com.danielsan.natapi.helpers.FileHandler

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global

trait FileRepository extends Repository {
  implicit class URI(uri: String) {
    override def toString = uri
  }

  def save(file: FileHandler, folder: String): (String, Future[Unit])
  def load(path: Path): String
}

class FileRepositoryImpl(rootFolder: String) extends FileRepository {

  override def prepare(): Future[Unit] = {
    Future { Paths.get(rootFolder).toFile.mkdirs() }
  }

  override def save(file: FileHandler, folder: String): (String, Future[Unit]) = {
    val filename = s"${UUID.randomUUID().toString}.${file.fileType.extension}"

    val path = Paths.get(rootFolder, folder, filename)

    (filename, file.saveToDisk(path))
  }

  override def load(path: Path): String = { "1" }
}
