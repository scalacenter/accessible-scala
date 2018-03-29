package ch.epfl.scala.accessible

import scala.meta._

import java.nio.file.{Files, Path}
import java.nio.charset.StandardCharsets

object Summary {

  def apply(tree: Tree): String =
    visitNames(tree, None)

  def apply(tree: Tree, position: Option[Position]): String =
    visitNames(tree, position)

  def apply(path: Path): String = apply(path, None)

  def apply(path: Path, position: Option[Position]): String = {
    val text = new String(Files.readAllBytes(path), StandardCharsets.UTF_8)
    val input = Input.String(text)
    apply(input.parse[Source].get, position)
  }

  private val nl = "\n"

  private def spaces(in: String): String = in.replaceAllLiterally(".", " ")

  private def visitNamesStats(stats: List[Tree],
                              fun: Tree => String,
                              end: Boolean = false): String = {
    val ends = if (end) "." else ""
    stats.map(fun).filter(_.nonEmpty).mkString("", "," + nl, ends)
  }

  private def visitNames(tree: Tree, position: Option[Position]): String = {
    position match {
      case Some(pos) => 
        findPosition(tree, pos) match {
          case Some(subtree) => {
            childrens(subtree)
          }
          case None => "cannot find tree"
        }
      case None => visitNames(tree)
    }
  }

  private def findPosition(tree: Tree, position: Position): Option[Tree] = {
    var found: Option[Tree] = None
    object findPos extends Traverser {
      override def apply(tree: Tree): Unit = {
        if(tree.pos.start == position.start &&
           tree.pos.end == position.end) {
          found = Some(tree)
        } else {
          super.apply(tree)
        }
      }
    }
    findPos(tree)
    found
  }

  private def childrens(tree: Tree): String = tree match {
    case Defn.Object(_, name, Template(_, _, _, stats)) =>
      s"object ${name.value}:" + nl +
        visitNamesStats(stats, visitDefiniton, end = true)
    case e => 
      s"cannot do ${e.getClass}"
  }

  private def visitDefiniton(subtree: Tree): String = subtree match {
    case Defn.Val(_, List(Pat.Var(name)), _, _) => s"val $name"
    case Defn.Var(_, List(Pat.Var(name)), _, _) => s"val $name"
    case t: Defn.Def => s"def ${t.name}"
    case t: Defn.Macro => s"macro ${t.name}"
    case t: Defn.Type => s"type ${t.name}"
    case t: Defn.Class => s"class ${t.name}"
    case t: Defn.Trait => s"trait ${t.name}"
    case t: Defn.Object => s"object ${t.name}"
  }
  
  private def visitNames(tree: Tree): String = tree match {
    case Source(stats) =>
      visitNamesStats(stats, visitNames, end = true)

    case Pkg(ref, stats) =>
      val name = spaces(ref.toString)
      s"package $name." + nl +
        visitNamesStats(stats, visitNames)

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
