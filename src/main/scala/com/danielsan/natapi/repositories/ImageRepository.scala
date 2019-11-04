package com.danielsan.natapi.repositories

import com.danielsan.natapi.models.Image
import com.twitter.finagle.mysql.{Client, LongValue, Result, Row, StringValue}
import com.twitter.util.Future

trait ImageRepository extends SQLRepository[Image] {
  def create(newImage: Image.New): Future[Result]
}

class ImageRepositoryImpl(implicit client: Client) extends SQLRepositoryImpl[Image] with ImageRepository {
  val tableName = "tb_images"

  private val query = loadQueryFromFile("CreateImageTable.sql")

  override def prepare(): Future[Result] = {
    client.query(query)
  }

  override def RowToModelType(row: Row): Image = {
    val LongValue(id) = row("id").get
    val StringValue(description) = row("description").get
    val StringValue(tags) = row("tags").get
    val StringValue(original_uri) = row("original_uri").get
    val StringValue(url) = row("url").get
    val LongValue(owner_id) = row("owner_id").get

    Image(id, description, tags.split(','), original_uri, url, owner_id)
  }

  override def create(newImage: Image.New): Future[Result] = {
    val tagString = newImage.tags.mkString(",")

    client.query(s"INSERT INTO $tableName (description,tags,owner_id) VALUES (${formatValueToQuery(newImage.description)}, ${formatValueToQuery(tagString)}, ${formatValueToQuery(newImage.owner_id)})")
  }
}
