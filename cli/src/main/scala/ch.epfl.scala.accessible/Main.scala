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
    val start = pos
    val end = pos

    val summary = s"summary $file".r
    val summaryAt = s"summary-at $pos $file".r
    val describe = s"describe $pos $file".r
    val breadcrumbs = s"breadcrumbs $pos $file".r
    
    val down = s"down $start $end $file".r
    val right = s"right $start $end $file".r
    val up = s"up $start $end $file".r
    val left = s"left $start $end $file".r

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
            case down(start, end, file) =>
              focus(start, end, file, _.down)
            case right(start, end, file) =>
              focus(start, end, file, _.right)
            case up(start, end, file) =>
              focus(start, end, file, _.up)
            case left(start, end, file) =>
              focus(start, end, file, _.left)
            case null =>
              running = false
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

  def focus(start: String, end: String, file: String, f: Cursor => Cursor): Unit = {
    val focus = Cursor(Paths.get(file), Range(start, end))
    val newFocus = f(focus)
    val selection = newFocus.current
    // val short = Cursor.shortName(newFocus.currentTree)
    println(s"select ${selection.start} ${selection.end}")
  }

  def err(t: Throwable): Unit = {
    // import java.io.{File, PrintStream}
    // val file = new File("test.err")
    // val ps = new PrintStream(file)
    // t.printStackTrace(ps)
    // ps.close()
  }
}
