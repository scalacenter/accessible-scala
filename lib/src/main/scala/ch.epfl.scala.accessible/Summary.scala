package ch.epfl.scala.accessible

import scala.meta._

import java.nio.file.{Files, Path}
import java.nio.charset.StandardCharsets

object Summary {

  def apply(tree: Tree): String =
    visitNames(tree)

  def apply(path: Path): String = {
    val text = new String(Files.readAllBytes(path), StandardCharsets.UTF_8)
    val input = Input.String(text)
    apply(input.parse[Source].get)
  }

  private val nl = "\n"

  private def spaces(in: String): String = in.replaceAllLiterally(".", " ")

  private def visitNamesStats(stats: List[Tree],
                              end: Boolean = false): String = {
    val ends = if (end) "." else ""
    stats.map(visitNames).filter(_.nonEmpty).mkString("", "," + nl, ends)
  }

  private def visitNames(tree: Tree): String = tree match {
    case Source(stats) =>
      visitNamesStats(stats, end = true)

    case Pkg(ref, stats) =>
      val name = spaces(ref.toString)
      s"package $name." + nl +
        visitNamesStats(stats)

    case pkgObj: Pkg.Object =>
      s"package object ${pkgObj.name}"

    case obj: Defn.Object =>
      s"object ${obj.name}"

    case cls: Defn.Class =>
      s"class ${cls.name}"

    case trt: Defn.Trait =>
      s"trait ${trt.name}"

    case imp: Import =>
      ""

    case e =>
      println(s"** Missing: ${e.getClass.toString} **")
      ""
  }
}
