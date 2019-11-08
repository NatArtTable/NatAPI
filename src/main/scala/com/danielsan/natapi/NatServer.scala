package com.danielsan.natapi

import com.danielsan.natapi.enconders.Enconders
import com.typesafe.config.{Config, ConfigFactory}
import com.twitter.finagle.Http
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import io.finch.{Application, Bootstrap}
import io.circe.generic.auto._
import io.finch.circe._

object NatServer extends Implementation with TwitterServer with Enconders {
  private implicit val conf: Config = ConfigFactory.load()
  private val port = conf.getInt("api.port")

  def start(): Unit = {
    // Preparing the service
    val service = Bootstrap
      .serve[Application.Json](api)
      .toService

    // serving HTTP
    log.info(s"Serving application on PORT: $port")
    val server = Http.server
      .withStatsReceiver(statsReceiver)
      .serve(s":$port", service)

    closeOnExit(server)

    Await.ready(server)
  }
}
