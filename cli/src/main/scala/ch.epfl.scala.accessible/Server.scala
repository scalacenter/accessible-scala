package ch.epfl.scala.accessible

import scala.io.StdIn.readLine
import java.nio.file.Paths
import java.io.InterruptedIOException
import scala.util.control.NonFatal

object Server {
  def apply(espeak: Espeak): Unit = {
    println("accessible-scala server started")
    var running = true
    while (running) {
      try {
        val line = readLine()
        val args = line.split(" ")
        Cli(espeak, args, fromServer = true)
      } catch {
        case e: InterruptedIOException => running = false
        case NonFatal(e)               => e.printStackTrace()
      }
    }
  }
}
