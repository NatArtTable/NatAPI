package com.danielsan.natapi

import com.danielsan.natapi.controllers._
import com.danielsan.natapi.endpoints.Authenticated
import com.danielsan.natapi.repositories._
import com.danielsan.natapi.services.{AuthService, UserService}
import com.typesafe.config.{Config, ConfigFactory}
import com.twitter.finagle.Http
import com.twitter.finagle.mysql.Client
import com.twitter.finagle.mysql.Transactions
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import io.finch.Application
import io.circe.generic.auto._
import io.finch.circe._

object NatApplication extends TwitterServer {
  // Loading Configuration file
  implicit val conf: Config = ConfigFactory.load()
  private val port = conf.getInt("api.port")

  // Database Configuration
  val mySqlClientBuilder = {
    new MySqlClientBuilder(conf)
  }
  private implicit val mySqlClient: Client with Transactions = mySqlClientBuilder.getClient

  // Loading repositories
  private val userRepository = new UserRepositoryImpl()
  private val imageRepository = new ImageRepositoryImpl()

  //Loading Services
  private val authService = new AuthService(userRepository)
  private val userService = new UserService(userRepository)

  //Loading Controllers
  private val userController = new UserController(userService)
  private val authController = new AuthController(authService)
  private val imageController = new ImageController(imageRepository)

  // Loading endpoints
  private lazy val api = userController.getEndpoints :+: imageController.getEndpoints :+: authController.getEndpoints

  def start(): Unit = {
//    // Preparing the server
    val server = Http.server
      .withStatsReceiver(statsReceiver)
      .serve(s":${port}", api.toServiceAs[Application.Json])
    closeOnExit(server)

    // Creating tables in the database
    Await.result(userRepository.prepare())
    Await.result(imageRepository.prepare())

    // Starting server
    Await.ready(server)
  }
}
