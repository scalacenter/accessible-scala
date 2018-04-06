package ch.epfl.scala.accessible

import scala.meta._
import java.nio.file.Path

object Describe {
  def apply(path: Path, offset: Offset): String =
    apply(parse(path), offset)

  def apply(tree: Tree, offset: Offset): String =
    find(tree, offset) match {
      case Some(subtree) => describe(subtree)
      case None          => "cannot find tree"
    }

  def describe(tree: Tree): String =
    tree match {
      case Type.Tuple(args) => "tuple: " + args.map(describe).mkString(", ")
      case Type.Name(value) => value
      case Type.Function(params, res) => {
        val dParams =
          if (params.nonEmpty) params.map(describe).mkString(", ")
          else "Unit"

        "function: " + dParams + " to " + describe(res)
      }

      case e => e.syntax
    }
}
