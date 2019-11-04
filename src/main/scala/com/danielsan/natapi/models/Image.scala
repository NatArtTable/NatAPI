package com.danielsan.natapi.models

case class Image(id: Long, description: String, tags: Seq[String], original_uri: String, uri: String, owner_id: Long)
