package com.danielsan.natapi.resources

import com.danielsan.natapi.models.Created

case class CreatedResource(id: Long)

object CreatedResource {
  def apply(created: Created): CreatedResource = CreatedResource(created.id)
}
