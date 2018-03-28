package ch.epfl.scala.accessible

import scala.io.StdIn.readLine
import java.nio.file.Paths

object Main {
  def main(args: Array[String]): Unit = {
    println("ready")
    val open = "open (.*)".r
    val move = "move (\\d+) (\\d+) (.*)".r
    while (true) {
      val line = readLine()
      line match {
        case open(path) =>
          Espeak(Summary(Paths.get(path)))
        case move(startS, endS, path) =>
          Espeak(Summary(Paths.get(path)))
        case e => Espeak(s"else: $e")
      }
    }
  }
}
