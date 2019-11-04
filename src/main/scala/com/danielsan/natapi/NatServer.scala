package com.danielsan.natapi

import com.danielsan.natapi.enconders.Enconders
import com.typesafe.config.{Config, ConfigFactory}
import com.twitter.finagle.Http
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import io.finch.Application
import io.circe.generic.auto._
import io.finch.circe._

object NatServer extends Configuration with TwitterServer with Enconders {
  // Loading Configuration file
  implicit val conf: Config = ConfigFactory.load()
  private val port = conf.getInt("api.port")

  prepare()

  def start(): Unit = {
    // Preparing the server
    val server = Http.server
      .withStatsReceiver(statsReceiver)
      .serve(s":$port", api.toServiceAs[Application.Json])
    closeOnExit(server)

    // Starting server
    Await.ready(server)
  }
}
