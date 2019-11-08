package com.danielsan.natapi.repositories

import com.cloudinary.Cloudinary
import com.danielsan.natapi.helpers.FileHandler

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global

trait FileRepository extends Repository {
  implicit class URI(uri: String) {
    override def toString = uri
  }

  def save(file: FileHandler): Future[String]
}

class FileRepositoryImpl(implicit var cloudinary: Cloudinary) extends FileRepository {
  override def prepare(): Future[Unit] = { Future.successful() }

  override def save(file: FileHandler): Future[String] = {
    val path = file.getFilePath

    cloudinary.uploader().upload(path.toAbsolutePath.toString) map { response =>
      response.url
    }
  }
}
