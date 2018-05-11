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

    val file = "(.*)"
    val pos = "(\\d+)"

    val summary = s"summary $file".r
    val summaryAt = s"summary-at $pos $file".r
    val describe = s"describe $pos $file".r
    val breadcrumbs = s"breadcrumbs $pos $file".r
    
    val down = s"down $pos $file".r
    val right = s"right $pos $file".r
    val up = s"up $pos $file".r
    val left = s"left $pos $file".r

    println("running")
    // espeak.synthesize("running " + scala.util.Random.nextInt(100))

    var running = true
    while (running) {
      try {
        val line = readLine()
        println("got: " + line)
        // espeak.stop()

        val output =
          line match {
            case summary(file) =>
              Summary(Paths.get(file))
            case summaryAt(pos, file) =>
              Summary(Paths.get(file), Offset(pos))
            case describe(pos, file) =>
              Describe(Paths.get(file), Offset(pos))
            case breadcrumbs(pos, file) =>
              Breadcrumbs(Paths.get(file), Offset(pos))
    
            case down(pos, file) =>
              println("match down")
              val focus = Focus(Paths.get(file), Offset(pos))
              println("focus 1")
              focus.down
              println("focus down")
              val selection = focus.current
              println("focus current")
              println(s"select ${selection.start} ${selection.end}")
              
            // case right(pos, file) =>
            // case up(pos, file) =>
            // case left(pos, file) =>
            case null =>
              running = false
              "closing."
            case e =>
              s"unknown command: $e"
          }

        // espeak.synthesize(output)
      } catch {
        case e: InterruptedIOException => {
          // espeak.synthesize("InterruptedIOException")
          running = false
        }
        case pe: ParseException => {
          // espeak.synthesize("Cannot parse")
        }
        case NonFatal(e) => {
          e.printStackTrace()
          // err(e)
        }
      }
    }
  }

  def err(t: Throwable): Unit = {
    // import java.io.{File, PrintStream}
    // val file = new File("test.err")
    // val ps = new PrintStream(file)
    // t.printStackTrace(ps)
    // ps.close()
  }
}
