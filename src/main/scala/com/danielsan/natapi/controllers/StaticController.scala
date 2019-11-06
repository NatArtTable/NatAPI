package com.danielsan.natapi.controllers

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import io.finch._

import org.slf4j.LoggerFactory

class StaticController extends Controller[Unit] {
  private val log = LoggerFactory.getLogger(this.getClass)

  private val getStaticContent: Endpoint[Unit] = get("static" :: paths[String]) { paths: Seq[String] =>
    val path = paths.mkString("/")
    log.debug(s"Static content in $path requested!")

    Future { Ok() } asTwitter
  }

  override protected def endpoints = getStaticContent
}
