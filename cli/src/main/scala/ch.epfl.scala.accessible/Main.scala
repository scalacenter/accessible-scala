package ch.epfl.scala.accessible

import espeak.Espeak
import scala.io.StdIn.readLine
import java.nio.file.Paths
import java.io.InterruptedIOException
import scala.util.control.NonFatal

import scala.meta.parsers.ParseException

object Main {

  def main(args: Array[String]): Unit = {
    System.loadLibrary("scala-espeak0")
    val espeak = new Espeak
    val open = "open (.*)".r
    val move = "move (\\d+) (\\d+) (.*)".r

    println("running")
    espeak.synthesize("running")
    var running = true
    while (running) {
      try {
        readLine() match {
          case open(path) =>
            espeak.stop()
            espeak.synthesize("open")
            espeak.synthesize(Summary(Paths.get(path)))
          case move(startS, endS, path) =>
            espeak.stop()
            espeak.synthesize("move")
            espeak.synthesize(Summary(Paths.get(path)))
          case null =>
            espeak.stop()
            espeak.synthesize("closing.")
            running = false
          case e =>
            espeak.stop()
            espeak.synthesize(s"else: $e")
        }
      } catch {
        case e: InterruptedIOException => {
          espeak.stop()
          espeak.synthesize("InterruptedIOException")
          running = false
        }
        case pe: ParseException => {
          espeak.stop()
          espeak.synthesize("Cannot parse")
        }
        case NonFatal(e) => {
          espeak.synthesize(e.getClass.toString)
        }
      }
    }
  }

  def err(t: Throwable): Unit = {
    import java.io.{File, PrintStream}
    val file = new File("test.err")
    val ps = new PrintStream(file)
    t.printStackTrace(ps)
    ps.close()
  }
}
