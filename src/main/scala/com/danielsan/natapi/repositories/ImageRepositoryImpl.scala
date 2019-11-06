package com.danielsan.natapi.repositories

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global

import slick.jdbc.MySQLProfile.api._

import DatabaseModels.ImageRow
import com.danielsan.natapi.models.{Created, Image, ImageModels, User}

class ImageRepositoryImpl(implicit val db: Database, implicit val images: TableQuery[ImageRow], implicit val fileRepository: FileRepository) extends ImageRepository {

  override def prepare(): Future[Unit] = {
    db.run(images.schema.create)
  }

  def getById(id: Long): Future[Option[Image]] = {
    db.run(images.filter(_.id === id).result.map(_.headOption)).map {
      case Some(row) => Some(Image(row.id, row.description, row.tags, row.original_uri, row.filename, row.owner_id))
      case None      => None
    }
  }

  override def getAllByOwnerId(owner_id: Long): Future[Seq[Image]] = {
    db.run(images.filter(_.owner_id === owner_id).result).map { rows =>
      rows.map(row => Image(row.id, row.description, row.tags, row.original_uri, row.filename, row.owner_id))
    }
  }

  override def create(newImage: ImageModels.New): Future[Created] = {
    val (filename, savingFile) = fileRepository.save(newImage.file, "images")

    val row = Image(1, newImage.description, newImage.tags, "", filename, newImage.owner_id)

    val savingInDatabase = db.run((images returning images.map(_.id)) += row) map { id =>
      Created(id)
    }

    for {
      _ <- savingFile
      resultDB <- savingInDatabase
    } yield resultDB
  }
}
