package com.danielsan.natapi.helpers

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future

import scala.util.matching.Regex

trait MimeTypeFilter {
  private val contentTypeMapping = Map(
    "jpeg" -> "image/jpg",
    "jpg" -> "image/jpg",
    "txt" -> "text/plain",
    "png" -> "image/png"
  )

  private val extensionRegex = new Regex(".+\\.([a-z]+)$")

  protected val mimeTypeFilter = new SimpleFilter[Request, Response] {
    override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
      service(request) map { response =>
        request.uri match {
          case extensionRegex(ext) =>
            contentTypeMapping.get(ext) match {
              case Some(contentType) => response.headerMap.add("Content-Type", contentType)
              case _                 =>
            }
          case _ =>
        }

        response
      }
    }
  }
}
