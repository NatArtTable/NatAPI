package com.danielsan.natapi.repositories

import com.danielsan.natapi.models.{Created, Image}
import com.twitter.finagle.mysql.{Client, EmptyValue, LongValue, OK, Result, Row, StringValue}
import com.twitter.util.Future

trait ImageRepository extends SQLRepository[Image] {
  def create(newImage: Image.New): Future[Created]
}

class ImageRepositoryImpl(implicit client: Client) extends SQLRepositoryImpl[Image] with ImageRepository {
  val tableName = "tb_images"

  private val query = loadQueryFromFile("CreateImageTable.sql")

  override def prepare(): Future[Result] = {
    client.query(query)
  }

  override def RowToModelType(row: Row): Image = {
    val LongValue(id) = row("id").get
    val LongValue(owner_id) = row("owner_id").get

    val description = row("description").map {
      case StringValue(v) => v
      case EmptyValue     => ""
    } getOrElse ""

    val tags = row("tags").map {
      case StringValue(v) => v
      case _              => ""
    } getOrElse ""

    val original_uri = row("original_uri").map {
      case StringValue(v) => v
      case _              => ""
    } getOrElse ""

    val uri = row("uri").map {
      case StringValue(v) => v
      case _              => ""
    } getOrElse ""

    Image(id, description, tags.split(','), original_uri, uri, owner_id)
  }

  override def create(newImage: Image.New): Future[Created] = {
    val tagString = newImage.tags.mkString(",")

    client.query(s"INSERT INTO $tableName (description,tags,owner_id) VALUES (${formatValueToQuery(newImage.description)}, ${formatValueToQuery(tagString)}, ${formatValueToQuery(newImage.owner_id)})") map {
      result =>
        Created(result.asInstanceOf[OK].insertId)
    }
  }
}
