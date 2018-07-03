package ch.epfl.scala.accessible

import scala.meta._

object Main {
  def main(args: Array[String]): Unit = {
    val espeak = new Espeak()

    if (args.headOption == Some("--server")) Server(espeak)
    else Cli(espeak, args, fromServer = false)
  }
}
