package com.danielsan.natapi.repositories

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import slick.jdbc.MySQLProfile.api._
import DatabaseModels.ImageRow
import com.danielsan.natapi.models.{Created, Image, ImageModels}

class ImageRepositoryImpl(implicit db: Database, implicit val images: TableQuery[ImageRow], implicit val fileRepository: FileRepository) extends ImageRepository {

  override def prepare(): Future[Unit] = {
    db.run(images.schema.create)
  }

  def getById(id: Long): Future[Option[Image]] = {
    db.run(images.filter(_.id === id).result.map(_.headOption)).map {
      case Some(row) => Some(Image(row.id, row.description, row.tags, row.original_uri, row.public_uri, row.owner_id, row.width, row.height))
      case None      => None
    }
  }

  override def getAllByOwnerId(owner_id: Long): Future[Seq[Image]] = {
    db.run(images.filter(_.owner_id === owner_id).result).map { rows =>
      rows.map(row => Image(row.id, row.description, row.tags, row.original_uri, row.public_uri, row.owner_id, row.width, row.height))
    }
  }

  override def create(newImage: ImageModels.New): Future[ImageModels.Created] = {
    for {
      publicURI <- fileRepository.save(newImage.file)
      id <- {
        val row = Image(1, newImage.description, newImage.tags, "", publicURI, newImage.owner_id, newImage.width, newImage.height)
        db.run((images returning images.map(_.id)) += row)
      }
    } yield { ImageModels.Created(id, publicURI) }
  }

  override def deleteById(id: Long): Future[Int] = {
    val q = images.filter(_.id === id)
    db.run(q.delete)
  }
}
