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

  def apply(tree: Tree, range: Range): String =
    find(tree, range) match {
      case Some(subtree) => describe(subtree)
      case None          => "cannot find tree"
    }

  def apply(tree: Tree): String = describe(tree)

  def describe(tree: Tree): String =
    tree match {
      case Defn.Class(mods, name, tparams, ctor, templ) => {
        val tparamsRes =
          if (tparams.nonEmpty) tparams.map(describe).mkString("parametrized with: ", ",", "")
          else ""

        s"${join(mods)} class ${describe(name)} $tparamsRes ${describe(ctor)} ${describe(templ)}"
      }

      // case Template(early: List[Stat], inits: List[Init], self: Self, stats: List[Stat])
      case Template(early, inits, self, stats) => ""
      // "template"

      // case Ctor.Primary(mods: List[Mod], name: Name, paramss: List[List[Term.Param]]) =>
      case Ctor.Primary(mods, _, paramss) => ""
      // "constructor"

      case Type.Name(value)         => value
      case Type.Project(qual, name) => s"${describe(qual)}, project, ${describe(name)}"
      case Type.Singleton(ref)      => s"singleton ${describe(ref)}"
      case Type.Apply(tpt, args) =>
        if (args.forall { case Type.Placeholder(Type.Bounds(None, None)) => true; case _ => false }) {
          s"${describe(tpt)} taking ${args.size} parameters"
        } else s"${describe(tpt)} of " + join(args)

      case Type.Tuple(args) => s"tuple ${args.size} of " + join(args)
      case Type.Function(params, res) => {
        val dParams =
          if (params.nonEmpty) join(params)
          else "Unit"

        "function " + dParams + " to " + describe(res)
      }

      case Type.Bounds(lower, higher) => {
        lower.map(l => "lower bounded by: " + describe(l)).getOrElse("") +
          higher.map(h => "upper bounded by: " + describe(h)).getOrElse("")
      }

      case Type.Param(mods, name, tparams, tbounds, vbounds, cbounds) => {

        val vboundsRes =
          if (vbounds.nonEmpty) vbounds.map(describe).mkString("view bounded by: ", ",", ".")
          else ""

        val cboundsRes =
          if (cbounds.nonEmpty) cbounds.map(describe).mkString("context bounded by: ", ",", ".")
          else ""

        val tparamsRes =
          if (tparams.nonEmpty) tparams.map(describe).mkString("of", ",", "")
          else ""

        List(
          join(mods),
          describe(name),
          tparamsRes,
          describe(tbounds),
          vboundsRes,
          cboundsRes
        ).filter(_.nonEmpty).mkString(" ")
      }

      case Mod.Covariant()     => "covariant"
      case Mod.Contravariant() => "contravariant"

      case e => ""
      // e.syntax
    }

  private def join(args: List[Tree]): String =
    args.map(describe).mkString(", ")
}
// class TreeMap, parametrized with: A view bound to Comparable of A, B
