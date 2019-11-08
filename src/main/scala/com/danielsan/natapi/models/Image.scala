package com.danielsan.natapi.models

import com.danielsan.natapi.helpers.FileHandler

case class Image(id: Long, description: String, tags: Seq[String], original_uri: String, public_uri: String, owner_id: Long)

object ImageModels {
  case class New(file: FileHandler, description: String, tags: Seq[String], owner_id: Long)
  case class Created(override val id: Long, public_uri: String) extends com.danielsan.natapi.models.Created
}
