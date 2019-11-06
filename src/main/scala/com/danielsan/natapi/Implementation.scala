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
import io.finch.Application
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

trait Implementation {
  private val log = LoggerFactory.getLogger(this.getClass)

  private implicit val conf: Config = ConfigFactory.load()

  // Configurations
  protected implicit val database: Database = Database.forConfig("db")
  private val rootFolder = conf.getString("filer.rootFolder")
  private val prepareTimeout = conf.getInt("db_preparation.timeout").seconds

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
  lazy val api = userController.getEndpoints :+: imageController.getEndpoints :+: authController.getEndpoints
  lazy val static = staticController.getEndpoints

  def prepare(): Unit = {
    log.info("Trying prepare the fileRepository")
    Try(Await.result(fileRepository.prepare(), prepareTimeout)) match {
      case Success(_) => log.info("Succeed in preparing the fileRepository")
      case Failure(e) => log.warn(s"Failed to prepare the fileRepository ${e.getMessage}")
    }

    log.info("Trying prepare the userRepository")
    Try(Await.result(userRepository.prepare(), prepareTimeout)) match {
      case Success(_) => log.info("Succeed in preparing the userRepository")
      case Failure(e) => log.warn(s"Failed to prepare the userRepository ${e.getMessage}")
    }

    log.info("Trying prepare the imageRepository")
    Try(Await.result(imageRepository.prepare(), prepareTimeout)) match {
      case Success(_) => log.info("Succeed in preparing the imageRepository")
      case Failure(e) => log.warn(s"Failed to prepare the imageRepository ${e.getMessage}")
    }

  }
}
