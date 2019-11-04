package com.danielsan.natapi.resources

import com.danielsan.natapi.models.Image

object ImageResource {

  case class Small(uri: String)
  object Small {
    def apply(image: Image): Small = Small(image.uri)
  }

  case class Full(uri: String, description: String, tags: scala.Seq[String])
  object Full {
    def apply(image: Image): Full = Full(image.uri, image.description, image.tags)
  }

  case class Create(base64enconded: String, description: String, tags: scala.Seq[String])

}
