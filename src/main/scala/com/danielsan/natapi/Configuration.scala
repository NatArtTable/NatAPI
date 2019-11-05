package com.danielsan.natapi

import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._
import com.typesafe.config.{Config, ConfigFactory}
import com.danielsan.natapi.controllers._
import com.danielsan.natapi.endpoints.{Authentication, AuthenticationImpl}
import com.danielsan.natapi.models.DatabaseModels
import com.danielsan.natapi.repositories._
import com.danielsan.natapi.services._

abstract class Configuration(dbConfigRoot: String) {
  import slick.jdbc.MySQLProfile.api._

  private implicit val conf: Config = ConfigFactory.load()

  // Database Configuration
  implicit val database: Database = Database.forConfig(dbConfigRoot)

  // File storage configuration
  private val imagesRootFolder = conf.getString("images.folder")

  // Loading repositories
  protected implicit val fileRepository: FileRepository = new FileRepositoryImpl(imagesRootFolder)
  protected implicit val userRepository: UserRepository = new UserRepositoryImpl()(database, DatabaseModels.users)
  protected implicit val imageRepository: ImageRepository = new ImageRepositoryImpl()(database, DatabaseModels.images, fileRepository)

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

  private val timeout: Duration = conf.getInt("timeouts.preparation").seconds
  protected def prepare(): Unit = {
    Await.result(fileRepository.prepare() recover { case e: Exception  => println("Failed to prepare file repository") }, timeout)
    Await.result(userRepository.prepare() recover { case e: Exception  => println("Failed to prepare user repository") }, timeout)
    Await.result(imageRepository.prepare() recover { case e: Exception => println("Failed to prepare image repository") }, timeout)
  }
}
