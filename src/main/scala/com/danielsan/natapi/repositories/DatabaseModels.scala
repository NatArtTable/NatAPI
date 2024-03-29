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
    def public_uri = column[String]("public_uri")
    def owner_id = column[Long]("owner_id")
    def width = column[Int]("width")
    def height = column[Int]("height")

    def owner = foreignKey("owner_fk", owner_id, users)(_.id)

    private def parseTags(s: String): Seq[String] = {
      s match {
        case ""        => Seq()
        case s: String => s.split(",")
      }
    }

    def * =
      ProvenShape.proveShapeOf(
        (id, description, tags_joined, original_uri, public_uri, owner_id, width, height) <> ((
            (id: Long,
             description: String,
             tags_joined: String,
             original_uri: String,
             public_uri: String,
             owner_id: Long,
             width: Int,
             height: Int) => Image(id, description, parseTags(tags_joined), original_uri, public_uri, owner_id, width, height)).tupled,
        (image: Image) => Option(image.id, image.description, image.tags.mkString(","), image.original_uri, image.public_uri, image.owner_id, image.width, image.height)))
  }

  implicit val images: TableQuery[ImageRow] = TableQuery[ImageRow]
}
