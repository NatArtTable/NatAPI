package com.danielsan.natapi.repositories

import com.danielsan.natapi.models.Image
import com.twitter.finagle.mysql.{Client, LongValue, Result, Row, StringValue}
import com.twitter.util.Future

trait ImageRepository extends SQLRepository[Image] {}

class ImageRepositoryImpl(implicit client: Client) extends SQLRepositoryImpl[Image] with ImageRepository {
  val tableName = "tb_images"

  private val query = loadQueryFromFile("CreateImageTable.sql")

  def prepare(): Future[Result] = {
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
}
