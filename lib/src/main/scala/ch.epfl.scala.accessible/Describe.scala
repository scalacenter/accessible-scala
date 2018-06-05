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
      // Ctor.Primary(mods: List[Mod], name: Name, paramss: List[List[Term.Param]]) =>
      case Ctor.Primary(mods, _, paramss) =>
        ""

      case Defn.Class(mods, name, tparams, ctor, templ) => {
        val tparamsRes =
          if (tparams.nonEmpty) tparams.map(describe).mkString("parametrized with: ", ",", "")
          else ""

        s"${join(mods)} class ${describe(name)} $tparamsRes ${describe(ctor)} ${describe(templ)}"
      }

      case Defn.Def(mods, name, tparams, paramss, decltpe, body) =>
        dDef(mods, name, tparams, paramss, decltpe, Some(body))

      case Decl.Def(mods, name, tparams, paramss, decltpe) =>
        dDef(mods, name, tparams, paramss, Some(decltpe), None)

      case Mod.Covariant()     => "co-variant"
      case Mod.Contravariant() => "contra-variant"

      // Template(early: List[Stat], inits: List[Init], self: Self, stats: List[Stat])
      case Template(early, inits, self, stats) =>
        ""
      case Term.Name(value) =>
        value
      case Term.Param(mods, name, decltpe, default) =>
        List(
          join(mods),
          describe(name),
          decltpe.map(describe).getOrElse(""),
          default.map(describe).getOrElse("")
        ).filter(_.nonEmpty).mkString(" ")

      case Type.Annotate(tpe, annots) =>
        s"${describe(tpe)} annotated with: ${join(annots)}"

      case Type.Apply(tpt, args) =>
        val allPlaceholders = args.forall {
          case Type.Placeholder(Type.Bounds(None, None)) => true
          case _ => false
        }

        if (allPlaceholders) s"${describe(tpt)} taking ${args.size} parameters"
        else {
          val verb =
            if (args.size == 1) "of"
            else "applied to"

          List(
            describe(tpt),
            verb,
            join(args)
          ).filter(_.nonEmpty).mkString(" ")
        }

      case Type.ApplyInfix(lhs, op, rhs) =>
        s"${describe(lhs)} ${describe(op)} ${describe(rhs)}"

      case Type.Bounds(lower, higher) => {
        List(
          lower.map(l => "super-type of: " + describe(l)).getOrElse(""),
          higher.map(h => "sub-type of: " + describe(h)).getOrElse("")
        ).filter(_.nonEmpty).mkString(", ")
      }

      case Type.ByName(tpe) =>
        "by name: " + describe(tpe)

      case Type.Name(value) =>
        value

      case Type.Function(params, res) => {
        val dParams =
          if (params.nonEmpty) join(params)
          else "Unit"

        "function " + dParams + " to " + describe(res)
      }
      case Type.Param(mods, name, tparams, tbounds, vbounds, cbounds) => {

        val vboundsRes =
          if (vbounds.nonEmpty) vbounds.map(describe).mkString("view bounded by: ", ", ", ".")
          else ""

        val cboundsRes =
          if (cbounds.nonEmpty) cbounds.map(describe).mkString("context bounded by: ", ", ", ".")
          else ""

        val tparamsRes =
          if (tparams.nonEmpty) tparams.map(describe).mkString("of", ", ", "")
          else ""

        val nameRes =
          if (name.is[Name.Anonymous]) "a parameter"
          else describe(name)

        List(
          join(mods),
          nameRes,
          tparamsRes,
          describe(tbounds),
          vboundsRes,
          cboundsRes
        ).filter(_.nonEmpty).mkString(" ")
      }
      case Type.Project(qual, name) =>
        s"${describe(qual)} project ${describe(name)}"

      // class Type.Placeholder(bounds: Bounds)

      case Type.Select(qual, name) =>
        describe(qual) + " dot " + describe(name)
      case Type.Singleton(ref) =>
        s"singleton ${describe(ref)}"
      case Type.Tuple(args) =>
        s"tuple ${args.size} of " + join(args)

      case Type.Refine(tpe, stats) =>
        List(
          "type refinement: ",
          tpe.map(describe).getOrElse(""),
          join(stats)
        ).filter(_.nonEmpty).mkString(" ")

      case Type.Repeated(tpe) =>
        "repeated: " + describe(tpe)

      // class Var(name: Name)

      case Type.With(lhs, rhs) =>
        s"${describe(lhs)} with ${describe(rhs)}"

      // Dotty
      // class Lambda(tparams: List[Type.Param], tpe: Type)
      // class ImplicitFunction(params: List[Type], res: Type)
      // class And(lhs: Type, rhs: Type)
      // class Or(lhs: Type, rhs: Type)

      // // Synthetic
      // class Method(paramss: List[List[Term.Param]], tpe: Type)

      // // Todo
      // class Existential(tpe: Type, stats: List[Stat])

      // case e => e.syntax
      case _ => ""
    }

  private def dDef(mods: List[Mod],
                   name: Term.Name,
                   tparams: List[scala.meta.Type.Param],
                   paramss: List[List[Term.Param]],
                   decltpe: Option[scala.meta.Type],
                   body: Option[Term]): String = {

    val tparamsRes =
      if (tparams.nonEmpty) tparams.map(describe).mkString("parametrized with: ", ", ", "")
      else ""

    val paramssRes =
      if (paramss.nonEmpty) paramss.flatMap(_.map(describe)).mkString(", ")
      else ""

    List(
      join(mods),
      "def",
      describe(name),
      tparamsRes,
      paramssRes
    ).filter(_.nonEmpty).mkString(" ") +
      List(
        decltpe.map(tpe => ".\nreturns: " + describe(tpe)).getOrElse(""),
        body.map(b => ".\nbody: " + describe(b)).getOrElse(""),
      ).filter(_.nonEmpty).mkString(" ")
  }

  private def join(args: List[Tree]): String =
    args.map(describe).mkString(", ")

}
// class TreeMap, parametrized with: A view bound to Comparable of A, B
