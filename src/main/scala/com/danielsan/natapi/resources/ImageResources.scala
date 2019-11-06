package com.danielsan.natapi.resources

import com.danielsan.natapi.helpers.FileHandler
import com.danielsan.natapi.models.Image

object ImageResources {

  case class Small(id: Long, filename: String)
  object Small {
    def apply(image: Image): Small = Small(image.id, image.filename)
  }

  case class Full(id: Long, filename: String, description: String, tags: Seq[String])
  object Full {
    def apply(image: Image): Full = Full(image.id, image.filename, image.description, image.tags)
  }

  case class Create(file: FileHandler, description: Option[String], tags: Option[Seq[String]])
}
