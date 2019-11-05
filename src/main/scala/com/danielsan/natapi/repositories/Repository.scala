package com.danielsan.natapi.repositories

import scala.concurrent.Future

trait Repository {
  def prepare(): Future[Unit]
}
