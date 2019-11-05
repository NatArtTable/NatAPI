package com.danielsan.natapi.resources

import com.danielsan.natapi.helpers.FileHandler
import com.danielsan.natapi.models.Image

object ImageResource {

  case class Small(id: Long, uri: String)
  object Small {
    def apply(image: Image): Small = Small(image.id, image.uri)
  }

  case class Full(id: Long, uri: String, description: String, tags: Seq[String])
  object Full {
    def apply(image: Image): Full = Full(image.id, image.uri, image.description, image.tags)
  }

  case class Create(file: FileHandler, description: Option[String], tags: Option[Seq[String]])
}
