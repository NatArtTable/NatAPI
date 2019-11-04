package com.danielsan.natapi.resources

import com.danielsan.natapi.models.Image

case class ImageResource(uri: String)

object ImageResource {
  def apply(image: Image): ImageResource = ImageResource(image.uri)
}
