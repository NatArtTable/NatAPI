package com.danielsan.natapi

import com.danielsan.natapi.enconders.Enconders
import com.typesafe.config.{Config, ConfigFactory}
import com.twitter.finagle.Http
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import io.finch.Application
import io.circe.generic.auto._
import io.finch.circe._
import slick.jdbc.MySQLProfile.api.Database

import scala.concurrent.duration._

object NatServer extends TwitterServer with Enconders {
  private implicit val conf: Config = ConfigFactory.load()
  private val port = conf.getInt("api.port")

  // Database Configuration
  implicit val database: Database = Database.forConfig("db")

  private val impl = new Implementation(database, conf.getString("images.folder"), conf.getInt("timeouts.preparation").seconds)

  impl.prepare()

  def start(): Unit = {
    // Preparing the server
    val server = Http.server
      .withStatsReceiver(statsReceiver)
      .serve(s":$port", impl.api.toServiceAs[Application.Json])
    closeOnExit(server)

    // Starting server
    Await.ready(server)
  }
}
