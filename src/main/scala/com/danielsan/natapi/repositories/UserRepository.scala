package com.danielsan.natapi.repositories

import com.danielsan.natapi.models.User
import com.twitter.finagle.mysql.{Client, LongValue, Result, Row, StringValue}
import com.twitter.util.Future

trait UserRepository extends SQLRepository[User] {
  def getByEmail(email: String): Future[Option[User]]
}

class UserRepositoryImpl(implicit client: Client) extends SQLRepositoryImpl[User] with UserRepository {
  val tableName = "tb_users"

  private val query = loadQueryFromFile("CreateUserTable.sql")

  def prepare(): Future[Result] = {
    client.query(query)
  }

  override def RowToModelType(row: Row): User = {
    val LongValue(id) = row("id").get
    val StringValue(name) = row("name").get
    val StringValue(email) = row("email").get
    val StringValue(password) = row("password").get

    User(id, name, email, password)
  }

  override def getByEmail(email: String): Future[Option[User]] = {
    client.select(s"SELECT * FROM $tableName WHERE $tableName.email = '$email'")(RowToModelType) map (_.headOption)
  }

}
