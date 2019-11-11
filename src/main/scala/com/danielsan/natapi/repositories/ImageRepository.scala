package com.danielsan.natapi.repositories

import com.danielsan.natapi.models.{ImageModels, Image}

import scala.concurrent.Future

trait ImageRepository extends Repository {
  def getById(id: Long): Future[Option[Image]]
  def getAllByOwnerId(owner_id: Long): Future[Seq[Image]]

  def create(newImage: ImageModels.New): Future[ImageModels.Created]
  def deleteById(id: Long): Future[Int]
}
