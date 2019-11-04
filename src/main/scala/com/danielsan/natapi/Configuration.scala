package com.danielsan.natapi

import com.twitter.finagle.Mysql
import com.twitter.finagle.mysql.{Client, Cursors, Result, Transactions}
import com.twitter.util.Await
import com.typesafe.config.{Config, ConfigFactory}
import com.danielsan.natapi.controllers._
import com.danielsan.natapi.endpoints.{Authentication, AuthenticationImpl}
import com.danielsan.natapi.repositories._
import com.danielsan.natapi.services._

trait Configuration {
  private implicit val conf: Config = ConfigFactory.load()

  // Database Configuration
  private implicit val mySqlClient: Client with Transactions with Cursors = Mysql.client
    .withCredentials(conf.getString("mysql.user"), conf.getString("mysql.password"))
    .withDatabase(conf.getString("mysql.db"))
    .newRichClient("%s:%d".format(conf.getString("mysql.host"), conf.getInt("mysql.port")))

  // Loading repositories
  protected implicit val userRepository: UserRepository = new UserRepositoryImpl()
  protected implicit val imageRepository: ImageRepository = new ImageRepositoryImpl()

  //Loading Services
  protected implicit val authService: AuthService = new AuthServiceImpl()
  protected implicit val userService: UserService = new UserServiceImpl()
  protected implicit val imageService: ImageService = new ImageServiceImpl()

  // Loading Endpoints
  private implicit val authenticated: Authentication = new AuthenticationImpl()

  //Loading Controllers
  protected val userController = new UserController()
  protected val authController = new AuthController()
  protected val imageController = new ImageController()

  // Loading the api
  protected lazy val api = userController.getEndpoints :+: imageController.getEndpoints :+: authController.getEndpoints

  protected def prepare(): Seq[Result] = {
    Seq(Await.result(userRepository.prepare()), Await.result(imageRepository.prepare()))
  }
}
