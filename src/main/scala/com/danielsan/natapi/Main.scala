package com.danielsan.natapi

object Main extends App {
  override def main(args: Array[String]): Unit = {
    if (args.length > 1) {
      println("You must supply exact one argument! (options: prepare | api)")
      sys.exit(1)
    } else if (args.length == 0) {
      println("You must supply exact one argument! (options: prepare | api)")
      sys.exit(1)
    } else {
      args(0) match {
        case "api"     => NatServer.start()
        case "prepare" => NatServer.prepare()
        case _ =>
          println("Invalid argument! (options: prepare | api)")
          sys.exit(1)
      }
    }
  }
}
