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

class Implementation(database: Database, imagesRootFolder: String, prepareTimeout: Duration) {

  // Loading repositories
  implicit val fileRepository: FileRepository = new FileRepositoryImpl(imagesRootFolder)
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
    Await.result(fileRepository.prepare() recover { case e: Exception  => println("Failed to prepare file repository") }, prepareTimeout)
    Await.result(userRepository.prepare() recover { case e: Exception  => println("Failed to prepare user repository") }, prepareTimeout)
    Await.result(imageRepository.prepare() recover { case e: Exception => println("Failed to prepare image repository") }, prepareTimeout)
  }
}
