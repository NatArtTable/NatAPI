package com.danielsan.natapi

import com.cloudinary.Cloudinary

import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration._
import com.danielsan.natapi.controllers._
import com.danielsan.natapi.filters.{Authentication, AuthenticationImpl}
import com.danielsan.natapi.repositories._
import com.danielsan.natapi.services._
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

class Implementation {
  private val log = LoggerFactory.getLogger(this.getClass)

  private implicit val conf: Config = ConfigFactory.load()

  // Configurations
  protected implicit val database: Database = Database.forConfig("db")
  private val prepareTimeout = conf.getInt("db_preparation.timeout").seconds

  // Cloudinary Configuration
  protected implicit val cloudinary: Cloudinary = try {
    new Cloudinary(conf.getString("file_handler.cloudinary"))
  } catch {
    case _: Exception => null
  }

  // Loading repositories
  implicit val fileRepository: FileRepository = new FileRepositoryImpl()
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

  // Loading the api
  lazy val api = userController.getEndpoints :+: imageController.getEndpoints :+: authController.getEndpoints

  def prepare(): Unit = {
    log.info("Trying prepare the fileRepository")
    Try(Await.result(fileRepository.prepare(), prepareTimeout)) match {
      case Success(_) => log.info("Succeed in preparing the fileRepository")
      case Failure(e) => log.warn(s"Failed to prepare the fileRepository ${e.getMessage}")
    }

    log.info("Trying prepare the userRepository")
    Try(Await.result(userRepository.prepare(), prepareTimeout)) match {
      case Success(_) => log.info("Succeed in preparing the userRepository")
      case Failure(e) => log.warn(s"Failed to prepare the userRepository. Message: ${e.getMessage}")
    }

    log.info("Trying prepare the imageRepository")
    Try(Await.result(imageRepository.prepare(), prepareTimeout)) match {
      case Success(_) => log.info("Succeed in preparing the imageRepository")
      case Failure(e) => log.warn(s"Failed to prepare the imageRepository. Message: ${e.getMessage}")
    }
  }
}
