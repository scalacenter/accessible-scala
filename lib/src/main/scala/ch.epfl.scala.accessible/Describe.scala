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

  def describe(tree: Tree): String = describeTree(tree)

  def describeTree(tree: Tree): String = tree match {
    case lit: Lit                              => describeLit(lit)
    case term: Term                            => describeTerm(term)
    case tpe: Type                             => describeType(tpe)
    case pat: Pat                              => describePat(pat)
    case decl: Decl                            => describeDecl(decl)
    case defn: Defn                            => describeDefn(defn)
    case mod: Mod                              => describeMod(mod)
    case enum: Enumerator                      => describeEnum(enum)
    case ctor: Ctor                            => describeCtor(ctor)
    case _: Import | _: Importer | _: Importee => describeImports(tree)
    case _                                     => describeMisc(tree)
  }

  def describeLit(lit: Lit): String = lit.syntax

  def describeTerm(term: Term): String = {
    import Term._

    term match {
      case Annotate(expr, annots) => ???
      case Apply(fun, args) => {
        s"${describe(fun)} applied to ${join(args)}"
      }
      case ApplyInfix(lhs, op, targs, args) => ???
      case ApplyType(fun, targs)            => ???
      case Ascribe(expr, tpe)               => ???
      case Assign(lhs, rhs)                 => ???
      case Block(stats)                     => ???
      case Do(body, expr)                   => ???
      case Eta(expr)                        => ???
      case For(enums, body)                 => ???
      case ForYield(enums, body)            => ???
      case Function(params, body)           => ???
      case If(cond, thenp, elsep)           => ???
      case Interpolate(prefix, parts, args) => ???
      case Match(expr, cases)               => ???
      case Name(value)                      => value
      case New(init)                        => ???
      case NewAnonymous(templ)              => ???
      // Term.Param see describeMisc
      case PartialFunction(cases) => {
        s"partial function ${join(cases)}"
      }
      case Placeholder()        => ???
      case Repeated(expr)       => ???
      case Return(expr)         => ???
      case Select(qual, name)   => ???
      case Super(thisp, superp) => ???
      case This(qual) => {
        qual match {
          case scala.meta.Name.Anonymous() => "this"
          case scala.meta.Name(value)      => s"this within $value"
        }
      }
      case Throw(expr)                            => ???
      case Try(expr, catchp, finallyp)            => ???
      case TryWithHandler(expr, catchp, finallyp) => ???
      case Tuple(args)                            => tuples(args)
      case While(expr, body)                      => ???
      case Xml(parts, args)                       => ???
    }
  }

  def describeType(tpe: Type): String = {
    import Type._

    def function(params: List[Type], res: Type): String = {
      val dParams =
        if (params.nonEmpty) join(params)
        else "Unit"

      "function " + dParams + " to " + describe(res)
    }

    tpe match {
      case And(lhs, rhs) => {
        s"${describe(lhs)} and ${describe(rhs)}"
      }
      case Annotate(tpe, annots) => {
        s"${describe(tpe)} annotated with: ${join(annots)}"
      }
      case Apply(tpe, args) => {
        val allPlaceholders = args.forall {
          case Type.Placeholder(Type.Bounds(None, None)) => true
          case _                                         => false
        }

        if (allPlaceholders) s"${describe(tpe)} taking ${args.size} parameters"
        else {
          val verb =
            if (args.size == 1) "of"
            else "applied to"

          mkString(
            describe(tpe),
            verb,
            join(args)
          )
        }
      }
      case ApplyInfix(lhs, op, rhs) => {
        s"${describe(lhs)} ${describe(op)} ${describe(rhs)}"
      }
      // Type.Bounds see describeMisc
      case ByName(tpe) => {
        "by name: " + describe(tpe)
      }
      case Existential(tpe, stats) => {
        s"existential ${describe(tpe)} for some ${join(stats)}"
      }
      case Function(params, res) => {
        function(params, res)
      }
      case ImplicitFunction(params, res) => {
        "implicit " + function(params, res)
      }
      case Lambda(tparams, tpe) => {
        s"lambda ${join(tparams)} to ${describe(tpe)}"
      }
      case Name(value) => {
        value
      }
      case Or(lhs, rhs) => {
        s"${describe(lhs)} or ${describe(rhs)}"
      }
      // Type.Param see describeMisc
      case Placeholder(bounds) => {
        "placeholder " + describeMisc(bounds)
      }
      case Project(qual, name) => {
        s"project ${describe(qual)} ${describe(name)}"
      }
      case Refine(tpe, stats) => {
        mkString(
          "type refinements:",
          option(tpe),
          join(stats)
        )
      }
      case Repeated(tpe) => {
        "repeated: " + describe(tpe)
      }
      case Select(qual, name) => {
        mkString(
          "select",
          describe(qual),
          describe(name)
        )

      }
      case Singleton(ref) => {
        s"singleton ${describe(ref)}"
      }
      case Tuple(args) => tuples(args)
      // case Var(name) => describeType(name)
      case With(lhs, rhs) => {
        s"${describe(lhs)} with ${describe(rhs)}"
      }
    }
  }

  def describePat(pat: Pat): String = {
    import Pat._

    def interpolation(prefix: String, parts: List[Tree], args: List[Tree]): String = {
      val body =
        args.zip(parts.tail).foldLeft(describe(parts.head)) {
          case (acc, (l, r)) =>
            val dr = describe(r)

            val sep0 =
              if (acc.endsWith(" ")) ""
              else " "

            val sep =
              if (dr.startsWith(" ")) ","
              else if (dr.isEmpty) ""
              else ", "

            acc + sep0 + "extracts " + describe(l) + sep + dr
        }

      s"$prefix interpolation $body"
    }

    pat match {
      case Alternative(lhs, rhs)            => s"${describe(lhs)} or ${describe(rhs)}"
      case Bind(lhs, rhs)                   => s"${describe(lhs)} bound to ${describe(rhs)}"
      case Extract(fun, args)               => s"${describe(fun)} extracts ${join(args)}"
      case ExtractInfix(lhs, op, rhs)       => s"${describe(lhs)} ${describe(op)} ${join(rhs)}"
      case Interpolate(prefix, parts, args) => interpolation(describe(prefix), parts, args)
      case SeqWildcard()                    => "multiple placeholders"
      case Tuple(args)                      => tuples(args)
      case Typed(lhs, rhs)                  => s"${describe(lhs)} typed ${describe(rhs)}"
      case Var(name)                        => describe(name)
      case Wildcard()                       => "placeholder"
      case Xml(parts, args)                 => interpolation("xml", parts, args)
      case Term.Name(name)                  => s"exactly $name"
    }
  }

  def describeDecl(decl: Decl): String = {
    import Decl._

    decl match {
      case Def(mods, name, tparams, paramss, decltpe) => {
        dDef(mods, name, tparams, paramss, Some(decltpe), None)
      }
      case Type(mods, name, tparams, bounds) => ???
      case Val(mods, pats, decltpe)          => ???
      case Var(mods, pats, decltpe)          => ???
    }
  }

  def describeDefn(defn: Defn): String = {
    import Defn._

    defn match {
      case Class(mods, name, tparams, ctor, templ) => {
        val tparamsRes =
          if (tparams.nonEmpty) tparams.map(describe).mkString("parametrized with: ", ",", "")
          else ""

        mkString(
          join(mods),
          "class",
          describeType(name),
          tparamsRes,
          describe(ctor),
          describe(templ)
        )
      }
      case Def(mods, name, tparams, paramss, decltpe, body) => {
        dDef(mods, name, tparams, paramss, decltpe, Some(body))
      }
      case Macro(mods, name, tparams, paramss, decltpe, body) => ???
      case Object(mods, name, templ)                          => ???
      case Trait(mods, name, tparams, ctor, templ)            => ???
      case Type(mods, name, tparams, body)                    => ???
      case Val(mods, pats, decltpe, rhs)                      => ???
      case Var(mods, pats, decltpe, rhs)                      => ???
    }
  }

  def describeMod(mod: Mod): String = {
    import Mod._

    def scoped(modifier: String, within: Ref): String = {
      val scope =
        within match {
          case Name.Anonymous() => ""
          case Name(v)          => s"within $v"
          case Term.This(_)     => "this"
        }

      mkString(modifier, scope)
    }

    mod match {
      case Annot(init)         => ???
      case Mod.Covariant()     => "co-variant"
      case Mod.Contravariant() => "contra-variant"
      case Private(within)     => scoped("private", within)
      case Protected(within)   => scoped("protected", within)
      case _                   => mod.syntax
    }
  }

  def describeEnum(enum: Enumerator): String = {
    import Enumerator._

    enum match {
      case Generator(pat, rhs) => ???
      case Guard(cond)         => ???
      case Val(pat, rhs)       => ???
    }
  }

  def describeCtor(ctor: Ctor): String = {
    import Ctor._

    ctor match {
      case Primary(mods, name, paramss) => {
        ""
      }
      case Secondary(mods, name, paramss, init, stats) => ???
    }
  }

  def describeImports(tree: Tree): String = tree match {
    case Import(importers)             => ???
    case Importee.Name(name)           => ???
    case Importee.Rename(name, rename) => ???
    case Importee.Unimport(name)       => ???
    case Importee.Wildcard()           => ???
    case Importer(ref, importees)      => ???
  }

  def describeMisc(tree: Tree): String = tree match {
    case Case(pat, cond, body) => {
      mkString(
        "case",
        describePat(pat),
        cond.map(c => s"if ${describe(c)}").getOrElse(""),
        "then",
        describe(body)
      )
    }
    case Init(tpe, name, argss)        => ???
    case Pkg(ref, stats)               => ???
    case Pkg.Object(mods, name, templ) => ???
    case Self(name, decltpe)           => ???
    case Source(stats)                 => ???
    case Template(early, inits, self, stats) => {
      // todo
      ""
    }
    case Term.Param(mods, name, decltpe, default) => {
      mkString(
        join(mods),
        describeTree(name),
        option(decltpe),
        option(default)
      )
    }
    case Type.Bounds(lower, higher) => {
      mkString(
        lower.map(l => "super-type of: " + describe(l)).getOrElse(""),
        higher.map(h => "sub-type of: " + describe(h)).getOrElse("")
      )
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
        else describeTree(name)

      mkString(
        join(mods),
        nameRes,
        tparamsRes,
        describe(tbounds),
        vboundsRes,
        cboundsRes
      )
    }

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

    mkString(
      join(mods),
      "def",
      describeTree(name),
      tparamsRes,
      paramssRes,
      decltpe.map(tpe => "returns: " + describe(tpe)).getOrElse(""),
      body.map(b => "body: " + describe(b)).getOrElse("")
    )
  }

  private def tuples(args: List[Tree]): String =
    s"tuple ${args.size} of ${join(args)}"

  private def join(args: List[Tree]): String =
    args.map(describe).mkString(", ")

  private def option(opt: Option[Tree]): String =
    opt.map(describe).getOrElse("")

  private def mkString(parts: String*): String = parts.filter(_.nonEmpty).mkString(" ")
}
