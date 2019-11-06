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
import com.typesafe.config.{Config, ConfigFactory}

class StaticController(implicit repository: FileRepository) extends Controller[AsyncStream[Buf]] {
  private val log = LoggerFactory.getLogger(this.getClass)

  private val conf: Config = ConfigFactory.load()
  private val chunkSize = conf.getInt("api.static.chunkSize").kilobytes.inBytes.toInt

  private val getStaticContent: Endpoint[AsyncStream[Buf]] = get("static" :: paths[String]) { paths: Seq[String] =>
    val result = Future {
      log.debug("Static content requested!")
      val path = Paths.get(paths.mkString("/"))
      log.debug(s"Static content path: ${path.toString}")

      val file = repository.getFile(path)

      log.debug("Checking for the existence of the file")
      if (!file.exists) {
        log.debug("File not exists! Throwing FileNotFoundException")
        throw new controllers.Controller.FileNotFoundException(s"File in $path not found.")
      }

      log.debug("Creating a Reader fromFile path")
      val reader: Reader = Reader.fromFile(file)

      log.debug("Responding with AsyncStream")
      Ok(AsyncStream.fromReader(reader, chunkSize = chunkSize))
    }

    result.asTwitter
  }

  override protected def endpoints = getStaticContent
}
