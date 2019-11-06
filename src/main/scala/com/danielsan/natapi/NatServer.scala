package com.danielsan.natapi

import com.danielsan.natapi.enconders.Enconders
import com.danielsan.natapi.helpers.MimeTypeFilter
import com.typesafe.config.{Config, ConfigFactory}
import com.twitter.finagle.Http
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import io.finch.{Application, Bootstrap}
import io.circe.generic.auto._
import io.finch.circe._

object NatServer extends TwitterServer with Enconders with Implementation with MimeTypeFilter {
  private implicit val conf: Config = ConfigFactory.load()
  private val port = conf.getInt("api.port")

  prepare()

  def start(): Unit = {
    // Preparing the service
    val service = mimeTypeFilter andThen Bootstrap
      .serve[Application.OctetStream](static)
      .serve[Application.Json](api)
      .toService

    // serving HTTP
    val server = Http.server
      .withStatsReceiver(statsReceiver)
      .serve(s":$port", service)

    closeOnExit(server)

    // Starting server
    Await.ready(server)
  }
}
