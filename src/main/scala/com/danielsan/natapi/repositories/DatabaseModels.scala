package com.danielsan.natapi.repositories

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape

import com.danielsan.natapi.models._

object DatabaseModels {

  class UserRow(tag: Tag) extends Table[User](tag, "tb_users") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def email = column[String]("email", O.Unique, O.Length(100))
    def password = column[String]("password")

    def * = ProvenShape.proveShapeOf((id, name, email, password) <> (User.tupled, User.unapply))
  }

  implicit val users: TableQuery[UserRow] = TableQuery[UserRow]

  class ImageRow(tag: Tag) extends Table[Image](tag, "tb_images" + "") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def description = column[String]("description")
    protected def tags_joined = column[String]("tags")
    def original_uri = column[String]("original_uri")
    def uri = column[String]("uri")
    def owner_id = column[Long]("owner_id")

    def owner = foreignKey("owner_fk", owner_id, users)(_.id)

    def * =
      ProvenShape.proveShapeOf(
        (id, description, tags_joined, original_uri, uri, owner_id) <> (((id: Long,
                                                                          description: String,
                                                                          tags_joined: String,
                                                                          original_uri: String,
                                                                          uri: String,
                                                                          owner_id: Long) => Image(id, description, tags_joined.split(","), original_uri, uri, owner_id)).tupled,
        (image: Image) => Option(image.id, image.description, image.tags.mkString(","), image.original_uri, image.filename, image.owner_id)))
  }

  implicit val images: TableQuery[ImageRow] = TableQuery[ImageRow]
}
