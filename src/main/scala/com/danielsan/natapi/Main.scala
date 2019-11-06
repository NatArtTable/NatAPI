package com.danielsan.natapi

object Main extends App {
  override def main(args: Array[String]): Unit = {
    NatServer.prepare()
    NatServer.start()
  }
}
