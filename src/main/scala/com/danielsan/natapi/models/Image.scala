package com.danielsan.natapi.models

import com.danielsan.natapi.helpers.FileHandler

case class Image(id: Long, description: String, tags: Seq[String], original_uri: String, uri: String, owner_id: Long)

object ImageModels {
  case class New(file: FileHandler, description: String, tags: Seq[String], owner_id: Long)
}
