package com.danielsan.natapi

import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration._
import com.danielsan.natapi.controllers._
import com.danielsan.natapi.endpoints.{Authentication, AuthenticationImpl}
import com.danielsan.natapi.repositories._
import com.danielsan.natapi.services._
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory

trait Implementation {
  private val log = LoggerFactory.getLogger(this.getClass)

  private implicit val conf: Config = ConfigFactory.load()

  // Configurations
  protected implicit val database: Database = Database.forConfig("db")
  private val rootFolder = conf.getString("filer.rootFolder")
  private val prepareTimeout = conf.getInt("db_preparation.timeout").seconds
  private val allowPreparationFailue = conf.getBoolean("db_preparation.allow_failure")

  // Loading repositories
  implicit val fileRepository: FileRepository = new FileRepositoryImpl(rootFolder)
  implicit val userRepository: UserRepository = new UserRepositoryImpl()(database, DatabaseModels.users)
  implicit val imageRepository: ImageRepository = new ImageRepositoryImpl()(database, DatabaseModels.images, fileRepository)

  //Loading Services
  implicit val authService: AuthService = new AuthServiceImpl()
  implicit val userService: UserService = new UserServiceImpl()
  implicit val imageService: ImageService = new ImageServiceImpl()

  // Loading Endpoints
  private implicit val authenticated: Authentication = new AuthenticationImpl()

  //Loading Controllers
  val userController = new UserController()
  val authController = new AuthController()
  val imageController = new ImageController()
  val staticController = new StaticController()

  // Loading the api
  lazy val api = staticController.getEndpoints :+: userController.getEndpoints :+: imageController.getEndpoints :+: authController.getEndpoints

  def prepare(): Unit = {
    if (allowPreparationFailue) {
      Await.result(fileRepository.prepare() recover {
        case _: Exception => log.info("Failed to prepare file repository")
      }, prepareTimeout)
      Await.result(userRepository.prepare() recover {
        case _: Exception => log.warn("Failed to prepare user repository")
      }, prepareTimeout)
      Await.result(imageRepository.prepare() recover {
        case _: Exception => log.warn("Failed to prepare image repository")
      }, prepareTimeout)
    } else {
      Await.result(fileRepository.prepare(), prepareTimeout)
      Await.result(userRepository.prepare(), prepareTimeout)
      Await.result(imageRepository.prepare(), prepareTimeout)
    }
  }
}
