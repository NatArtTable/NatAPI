package com.danielsan.natapi.services

import com.danielsan.natapi.TestServer
import org.scalatest.{BeforeAndAfter, FunSpec}

import scala.concurrent.duration._

trait BaseSpec extends FunSpec with BeforeAndAfter {
  protected val server: TestServer = new TestServer(5.seconds)

  after {
    server.tearDown()
  }
}
