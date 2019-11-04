package com.danielsan.natapi

import com.twitter.finagle.Mysql
import com.twitter.finagle.mysql.{Client, Cursors, Result, Transactions}
import com.twitter.util.Await
import com.typesafe.config.{Config, ConfigFactory}

import com.danielsan.natapi.controllers._
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
  protected implicit val authService: AuthService = new AuthServiceImpl(userRepository)
  protected implicit val userService: UserService = new UserServiceImpl(userRepository)
  protected implicit val imageService: ImageService = new ImageServiceImpl(imageRepository)

  //Loading Controllers
  protected val userController = new UserController(userService)
  protected val authController = new AuthController(authService)
  protected val imageController = new ImageController(imageService)

  // Loading the api
  protected lazy val api = userController.getEndpoints :+: imageController.getEndpoints :+: authController.getEndpoints

  protected def prepare(): Seq[Result] = {
    Seq(Await.result(userRepository.prepare()), Await.result(imageRepository.prepare()))
  }
}
