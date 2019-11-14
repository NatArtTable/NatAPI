package com.danielsan.natapi.resources

import com.danielsan.natapi.helpers.FileHandler
import com.danielsan.natapi.models.{Image, ImageModels}

object ImageResources {

  case class Small(id: Long, public_uri: String, width: Int, height: Int)
  object Small {
    def apply(image: Image): Small = Small(image.id, image.public_uri, image.width, image.height)
  }

  case class Full(id: Long, public_uri: String, description: String, tags: Seq[String], width: Int, height: Int)
  object Full {
    def apply(image: Image): Full = Full(image.id, image.public_uri, image.description, image.tags, image.width, image.height)
  }

  case class Create(file: FileHandler, description: Option[String], tags: Option[Seq[String]], width: Int, height: Int)

  case class Created(id: Long, public_uri: String)
  object Created {
    def apply(image: ImageModels.Created): Created = Created(image.id, image.public_uri)
  }

  case class Deleted(id: Long)
}
