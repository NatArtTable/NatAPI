package com.danielsan.natapi.repositories

import com.twitter.finagle.mysql.{Client, Result, Row}
import com.twitter.util.Future

import scala.io.Source

trait SQLRepository[ModelType] {
  val tableName: String

  protected def RowToModelType(row: Row): ModelType

  protected def loadQueryFromFile(filename: String): String

  def getById(id: Long): Future[Option[ModelType]]
  def getAll(): Future[Seq[ModelType]]
}

abstract class SQLRepositoryImpl[ModelType](implicit client: Client) extends SQLRepository[ModelType] {

  def prepare(): Future[Result]

  override def loadQueryFromFile(filename: String): String = {
    val createTableSQLFileStream = getClass.getResourceAsStream(s"/sqls/$filename")
    Source.fromInputStream(createTableSQLFileStream).getLines().mkString("\n")
  }

  override def getById(id: Long): Future[Option[ModelType]] = {
    client.select(s"SELECT * FROM $tableName WHERE $tableName.id = $id")(RowToModelType) map (_.headOption)
  }

  override def getAll(): Future[Seq[ModelType]] = {
    client.select(s"SELECT * FROM $tableName")(RowToModelType)
  }
}
