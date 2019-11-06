package com.danielsan.natapi.controllers

import java.nio.file.Paths

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global

import com.twitter.conversions.storage._
import com.twitter.concurrent.AsyncStream
import com.twitter.io.{Buf, Reader}

import io.finch._

import org.slf4j.LoggerFactory

import com.danielsan.natapi.controllers
import com.danielsan.natapi.repositories.FileRepository

class StaticController(implicit repository: FileRepository) extends Controller[AsyncStream[Buf]] {
  private val log = LoggerFactory.getLogger(this.getClass)

  private val getStaticContent: Endpoint[AsyncStream[Buf]] = get("static" :: paths[String]) { paths: Seq[String] =>
    val result = Future {
      val path = Paths.get(paths.mkString("/"))
      log.debug(s"Static content in $path requested!")

      val file = repository.getFile(path)

      if (!file.exists) throw new controllers.Controller.FileNotFoundException(s"File $path not found.")

      val reader: Reader = Reader.fromFile(file)
      Ok(AsyncStream.fromReader(reader, chunkSize = 20.megabyte.inBytes.toInt))
    }

    result.asTwitter
  }

  override protected def endpoints = getStaticContent
}
