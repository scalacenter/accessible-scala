package ch.epfl.scala.accessible

import build.BuildInfo.{moduleName, version => ver}

import org.rogach.scallop._
import org.rogach.scallop.exceptions._

import java.io.File
import java.nio.file.{Files, Path}
import java.nio.charset.StandardCharsets

import scala.meta._

sealed trait CodeInput {
  def parse: Parsed[Tree]
}
object CodeInput {
  private def parse0(path: Path): Parsed[Tree] = {
    parse0(new String(Files.readAllBytes(path), StandardCharsets.UTF_8))
  }

  private def parse0(text: String): Parsed[Tree] = {
    val input = Input.String(text)
    input.parse[Source]
  }

  case object FromStdIn extends CodeInput {
    def parse: Parsed[Tree] = {
      val input = scala.io.Source.fromInputStream(System.in).mkString(System.lineSeparator)
      parse0(input)
    }
  }
  case class FromPath(path: Path) extends CodeInput {
    def parse: Parsed[Tree] = parse0(path)
  }
}

sealed trait CliCommand
object CliCommand {
  case class Summary(offset: Option[Offset]) extends CliCommand
  case class Describe(offset: Option[Offset]) extends CliCommand
  case class Breadcrumbs(offset: Offset) extends CliCommand
  case class Navigate(direction: Direction, range: Range) extends CliCommand
}

sealed trait Direction { def apply(in: Cursor): Cursor }
object Direction {
  case object Up extends Direction { def apply(in: Cursor): Cursor = in.up }
  case object Down extends Direction { def apply(in: Cursor): Cursor = in.down }
  case object Left extends Direction { def apply(in: Cursor): Cursor = in.left }
  case object Right extends Direction { def apply(in: Cursor): Cursor = in.right }
}

sealed trait OutputType
object OutputType {
  case object Voice extends OutputType
  case object Text extends OutputType
}

object CliOptions {
  def apply(args: Array[String]): Option[CliOptions] = {
    val conf = new Conf(args)
    conf.build
  }
}

case class CliOptions(command: CliCommand, code: CodeInput, output: OutputType)

class Conf(args: Seq[String]) extends ScallopConf(args) {

  override def onError(e: Throwable): Unit = e match {
    case Help("") =>
      builder.printHelp
    case Help(subname) =>
      builder.findSubbuilder(subname).get.printHelp
    case Version =>
      builder.vers.foreach(println)
  }

  trait Command { def toCommand: CliCommand }
  trait Base extends Command { _: ScallopConf =>
    val file = opt[File](
      name = "file",
      descr = "read code from a file path"
    )
    val stdin = opt[Boolean](
      name = "stdin",
      descr = "read code from stdin (piped)"
    )
    val output = choice(
      Seq("voice", "text"),
      name = "output",
      descr = "output text to pipe to text-to-speech or voice to use espeak",
      required = true
    )

    def toOption: CliOptions = {
      requireOne(stdin, file)

      def getOutput: OutputType = {
        output() match {
          case "voice" => OutputType.Voice
          case "text"  => OutputType.Text
          case _       => throw new Exception("impossible")
        }
      }
      def getCode: CodeInput =
        (file.toOption, stdin()) match {
          case (Some(f), false) => CodeInput.FromPath(f.toPath)
          case (None, true)     => CodeInput.FromStdIn
          case _                => throw new Exception("impossible")
        }

      CliOptions(toCommand, getCode, getOutput)
    }
  }
  trait OptionnalOffset { _: ScallopConf =>
    val offset = opt[Int](
      name = "offset",
      descr = "cursor position in offset"
    )
    def getOffset: Option[Offset] = offset.toOption.map(Offset(_))
  }

  val summary0 = new Subcommand("summary") with Base with OptionnalOffset {
    def toCommand: CliCommand = CliCommand.Summary(getOffset)
  }
  addSubcommand(summary0)

  val describe = new Subcommand("describe") with Base with OptionnalOffset {
    def toCommand: CliCommand = CliCommand.Describe(getOffset)
  }
  addSubcommand(describe)

  val breadcrumbs = new Subcommand("breadcrumbs") with Base {
    val offset = opt[Int](
      name = "offset",
      descr = "cursor position in offset"
    )
    def toCommand: CliCommand = CliCommand.Breadcrumbs(Offset(offset()))
  }
  addSubcommand(breadcrumbs)

  val navigate = new Subcommand("navigate") with Base {
    val start = opt[Int](
      name = "start",
      required = true,
      descr = "selection start in offset"
    )
    val end = opt[Int](
      name = "end",
      required = true,
      descr = "selection end in offset"
    )
    val direction = choice(
      Seq("up", "down", "left", "right"),
      name = "direction",
      required = true,
      descr = "where to navigate in the code ast"
    )

    def getDirection: Direction = {
      direction() match {
        case "up"    => Direction.Up
        case "down"  => Direction.Down
        case "left"  => Direction.Left
        case "right" => Direction.Right
      }
    }

    def toCommand: CliCommand = CliCommand.Navigate(getDirection, Range(start(), end()))
  }
  addSubcommand(navigate)

  version(s"$moduleName $ver (c) 2018 Scala-Center")
  banner(s"""|Usage: $moduleName
             |
             |Options:
             |""".stripMargin)
  footer("\nMore info at https://github.com/scalacenter/accessible-scala")

  verify()

  def build: Option[CliOptions] = subcommand.flatMap {
    case b: Base => Some(b.toOption)
    case _       => None
  }
}
