package com.danielsan.natapi.repositories

import com.twitter.finagle.mysql.{Client, Result, Row}
import com.twitter.util.Future

import scala.io.Source

trait SQLRepository[ModelType] {
  val tableName: String

  protected def RowToModelType(row: Row): ModelType
  protected def loadQueryFromFile(filename: String): String

  def prepare(): Future[Result]
  def getById(id: Long): Future[Option[ModelType]]
  def getAll(): Future[Seq[ModelType]]
  def filter(columnName: String, value: Any): Future[Seq[ModelType]]
  def filter(columnName: String, value: Any, limit: Int): Future[Seq[ModelType]]

}

abstract class SQLRepositoryImpl[ModelType](implicit client: Client) extends SQLRepository[ModelType] {

  override def loadQueryFromFile(filename: String): String = {
    val createTableSQLFileStream = getClass.getResourceAsStream(s"/sqls/$filename")
    Source.fromInputStream(createTableSQLFileStream).getLines().mkString("\n")
  }

  override def getById(id: Long): Future[Option[ModelType]] = {
    client.select(s"SELECT * FROM $tableName WHERE $tableName.id = $id LIMIT 1")(RowToModelType) map (_.headOption)
  }

  private def formatValueToQuery(value: Any): String = {
    value match {
      case v: Int    => v.toString
      case v: String => if (v.contains('\'')) { throw new IllegalArgumentException("value string cannot contains '") } else { s"\'$v\'" }
      case _         => throw new IllegalArgumentException("value should be of a supported type: (Int, String)")
    }
  }

  override def filter(columnName: String, value: Any): Future[Seq[ModelType]] = {
    client.select(s"SELECT * FROM $tableName WHERE $columnName = ${formatValueToQuery(value)}")(RowToModelType)
  }

  override def filter(columnName: String, value: Any, limit: Int): Future[Seq[ModelType]] = {
    client.select(s"SELECT * FROM $tableName WHERE $columnName = ${formatValueToQuery(value)} LIMIT $limit")(RowToModelType)
  }

  override def getAll(): Future[Seq[ModelType]] = {
    client.select(s"SELECT * FROM $tableName")(RowToModelType)
  }

}
