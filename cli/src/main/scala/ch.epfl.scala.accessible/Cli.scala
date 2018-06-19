package ch.epfl.scala.accessible

import scala.meta._
import scala.util.control.NonFatal

object Cli {
  def apply(espeak: Espeak, args: Array[String], fromServer: Boolean): Unit = {
    val cli = new Cli(espeak, fromServer)
    cli.run(args)
  }
}

class Cli(espeak: Espeak, fromServer: Boolean) {
  def run(args: Array[String]): Unit = {
    try {
      CliOptions(args).map(options =>
        options.code.parse match {
          case Parsed.Success(tree) => run(options, tree)
          case Parsed.Error(pos, message, _) => {
            val range = Range(pos.start, pos.end)
            printRange(range)
            printMessage(message, options.output)
          }
      })
    } catch {
      case NonFatal(e) => e.printStackTrace()
    }
  }

  private def run(options: CliOptions, tree: Tree): Unit = {
    val output =
      options.command match {
        case CliCommand.Summary(offset)     => Summary(tree, offset)
        case CliCommand.Describe(offset)    => Describe(tree, offset)
        case CliCommand.Breadcrumbs(offset) => Breadcrumbs(tree, offset)
        case CliCommand.Navigate(direction, range) => {
          val focus = direction(Cursor(tree, range))
          val focusRange = focus.current
          printRange(focusRange)
          Describe(focus.tree)
        }
      }
    printMessage(output, options.output)
  }

  private def printRange(range: Range): Unit = {
    println(s"select ${range.start} ${range.end}")
  }

  private def printMessage(message: String, output: OutputType): Unit = {
    output match {
      case OutputType.Voice => espeak(message, synchronize = !fromServer)
      case OutputType.Text  => println(message)
    }
  }
}
