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

  private def describe(tree: Tree): String = describeTree(tree)

  private def describeTree(tree: Tree): String = tree match {
    case lit: Lit                              => describeLit(lit)
    case term: Term                            => describeTerm(term)
    case tpe: Type                             => describeType(tpe)
    case pat: Pat                              => describePat(pat)
    case decl: Decl                            => describeDecl(decl)
    case defn: Defn                            => describeDefn(defn)
    case mod: Mod                              => describeMod(mod)
    case enum: Enumerator                      => describeEnum(enum)
    case _: Import | _: Importer | _: Importee => describeImports(tree)
    case _                                     => describeMisc(tree)
  }

  private def describeLit(lit: Lit): String = {
    import Lit._

    lit match {
      case Unit() => "unit"
      case _      => lit.syntax
    }
  }

  private def describeTerm(term: Term): String = {
    import Term._

    term match {
      case Annotate(expr, annots) => {
        s"${describe(expr)} annotated with: ${join(annots)}"
      }
      case Apply(fun, args) => {
        val funRes = describe(fun)
        if (args.nonEmpty) s"$funRes applied to ${join(args)}"
        else s"call to $funRes"
      }
      case ApplyInfix(lhs, op, targs, args) => {
        val targsPart =
          if (targs.nonEmpty) "parameterized with " + join(targs) + " applied to"
          else ""

        val argsPart =
          if (args.nonEmpty) join(args)
          else "empty arguments"

        mkString(
          describe(lhs),
          describe(op),
          targsPart,
          argsPart
        )
      }
      case ApplyType(fun, targs) => {
        val subject =
          if (targs.size > 1) "types"
          else "type"

        mkString(
          describe(fun),
          "applied to",
          subject,
          join(targs)
        )
      }

      case ApplyUnary(op, arg) => {
        describe(op) + " " + describe(arg)
      }

      case Ascribe(expr, tpe) => {
        s"${describe(expr)} typed as ${describe(tpe)}"
      }

      case Assign(lhs, rhs) => {
        s"${describe(lhs)} assigned to ${describe(rhs)}"
      }
      case Block(stats) => join(stats)
      case Do(body, expr) => {
        s"do ${describe(body)} while ${describe(expr)}"
      }
      case Eta(expr) => {
        s"η-conversion of ${describe(expr)}"
      }
      case For(enums, body) => {
        mkString(
          "for",
          join(enums),
          "do",
          describe(body)
        )
      }
      case ForYield(enums, body) => {
        mkString(
          "for",
          join(enums),
          "yield",
          describe(body)
        )
      }
      case Function(params, body) => {
        val dParams =
          if (params.nonEmpty) join(params)
          else "unit"

        "function " + dParams + " to " + describe(body)
      }
      case If(cond, thenp, elsep) => {
        val elseRes =
          if (elsep.is[Lit.Unit] && elsep.tokens.isEmpty) ""
          else s"else ${describe(elsep)}"

        mkString(
          s"if ${describe(cond)} then ${describe(thenp)}",
          elseRes
        )
      }
      case Interpolate(prefix, parts, args) =>
        interpolation(describe(prefix), "insert", parts, args)
      case Match(expr, cases) => {
        mkString(
          describe(expr),
          "match",
          join(cases)
        )
      }
      case Name(value)         => value
      case New(init)           => s"new ${describe(init)}"
      case NewAnonymous(templ) => s"new anonymous ${describe(templ)}"
      // Term.Param see describeMisc
      case PartialFunction(cases) => s"partial function ${join(cases)}"
      case Placeholder()          => "placeholder"
      case Repeated(expr)         => s"repeated ${describe(expr)}"
      case Return(expr)           => s"returns ${describe(expr)}"
      case Select(qual, name)     => s"${describe(qual)} dot ${describe(name)}"
      case Super(thisp, superp)   => mkString(describe(thisp), "super", describe(superp))
      case This(qual) => {

        val qualRes =
          qual match {
            case scala.meta.Name.Anonymous() => ""
            case scala.meta.Name(value)      => s"within $value"
          }

        mkString("this", qualRes)
      }
      case Throw(expr) => s"throw ${describe(expr)}"
      case Try(expr, catchp, finallyp) => {
        val catchpRes =
          if (catchp.nonEmpty) "catch " + join(catchp)
          else ""

        mkString(
          "try",
          describe(expr),
          catchpRes,
          finallyp.map(f => "finally " + describe(f)).getOrElse("")
        )

      }
      case TryWithHandler(expr, catchp, finallyp) => {
        mkString(
          "try",
          describe(expr),
          "catch",
          describe(catchp),
          finallyp.map(f => "finally " + describe(f)).getOrElse("")
        )
      }
      case Tuple(args)       => tuples(args)
      case While(expr, body) => s"while ${describe(expr)} do ${describe(body)}"
      case Xml(parts, args) =>
        joinParts("xml literal", "scala expression", parts, args, isInterpolation = false)
    }
  }

  private def describeType(tpe: Type): String = {
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
      // case Var(name) => describeType(name) // ???
      case With(lhs, rhs) => {
        s"${describe(lhs)} with ${describe(rhs)}"
      }
    }
  }

  private def describePat(pat: Pat): String = {
    import Pat._

    def patInterpolation(prefix: String, parts: List[Tree], args: List[Tree]): String =
      interpolation(prefix, "extracts", parts, args)

    pat match {
      case Alternative(lhs, rhs)            => s"${describe(lhs)} or ${describe(rhs)}"
      case Bind(lhs, rhs)                   => s"${describe(lhs)} bound to ${describe(rhs)}"
      case Extract(fun, args)               => s"${describe(fun)} extracts ${join(args)}"
      case ExtractInfix(lhs, op, rhs)       => s"${describe(lhs)} ${describe(op)} ${join(rhs)}"
      case Interpolate(prefix, parts, args) => patInterpolation(describe(prefix), parts, args)
      case SeqWildcard()                    => "multiple placeholders"
      case Tuple(args)                      => tuples(args)
      case Typed(lhs, rhs)                  => s"${describe(lhs)} typed as ${describe(rhs)}"
      case Var(name)                        => describe(name)
      case Wildcard()                       => "placeholder"
      case Xml(parts, args)                 => patInterpolation("xml", parts, args)
      case Term.Name(name)                  => s"exactly $name"
    }
  }

  // private def

  private def describeDecl(decl: Decl): String = {
    import Decl._

    decl match {
      case Def(mods, name, tparams, paramss, decltpe) => {
        describeDef(mods, name, tparams, paramss, Some(decltpe), None)
      }
      case Type(mods, name, tparams, bounds) => {
        val boundsSep =
          if (tparams.nonEmpty) "."
          else ""

        mkString(
          join(mods),
          "type",
          describe(name),
          describeTparams(tparams) + boundsSep,
          describe(bounds)
        )
      }
      case Val(mods, pats, decltpe) => {
        mkString(
          join(mods),
          "val",
          join(pats),
          describe(decltpe)
        )
      }
      case Var(mods, pats, decltpe) => {
        mkString(
          join(mods),
          "var",
          join(pats),
          describe(decltpe)
        )
      }
    }
  }

  private def describeDefn(defn: Defn): String = {
    import Defn._

    defn match {
      case Class(mods, name, tparams, ctor, templ) =>
        mkString(
          join(mods),
          "class",
          describeType(name),
          describeTparams(tparams),
          describe(ctor),
          describe(templ)
        )

      case Def(mods, name, tparams, paramss, decltpe, body) =>
        describeDef(mods, name, tparams, paramss, decltpe, Some(body))

      case Macro(mods, name, tparams, paramss, decltpe, body) =>
        describeDef(mods, name, tparams, paramss, decltpe, Some(body), isMacro = true)

      case Object(mods, name, templ) =>
        mkString(
          join(mods),
          "object",
          describe(name),
          describe(templ)
        )

      case Trait(mods, name, tparams, ctor, templ) =>
        mkString(
          join(mods),
          "trait",
          describeType(name),
          describeTparams(tparams),
          describe(ctor),
          describe(templ)
        )

      case Type(mods, name, tparams, body) =>
        mkString(
          join(mods),
          "type",
          describe(name),
          describeTparams(tparams),
          describe(body)
        )

      case Val(mods, pats, decltpe, rhs) =>
        mkString(
          join(mods),
          "val",
          join(pats),
          decltpe.map(tpe => "of type: " + describe(tpe)).getOrElse(""),
          "=",
          describe(rhs)
        )

      case Var(mods, pats, decltpe, rhs) =>
        mkString(
          join(mods),
          "var",
          join(pats),
          decltpe.map(tpe => "of type: " + describe(tpe)).getOrElse(""),
          "=",
          option(rhs)
        )
    }
  }

  private def describeMod(mod: Mod): String = {
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
      case Annot(init)         => describe(init)
      case Mod.Covariant()     => "co-variant"
      case Mod.Contravariant() => "contra-variant"
      case Private(within)     => scoped("private", within)
      case Protected(within)   => scoped("protected", within)
      case _                   => mod.syntax
    }
  }

  private def describeEnum(enum: Enumerator): String = {
    import Enumerator._

    enum match {
      case Generator(pat, rhs) => s"${describe(pat)} in ${describe(rhs)}"
      case Guard(cond)         => s"if ${describe(cond)}"
      case Val(pat, rhs)       => s"${describe(pat)} = ${describe(rhs)}"
    }
  }

  private def describeImports(tree: Tree): String = tree match {
    case Import(importers)             => "import: " + join(importers)
    case Importee.Name(name)           => describe(name)
    case Importee.Rename(name, rename) => s"rename ${describe(name)} to ${describe(rename)}"
    case Importee.Unimport(name)       => s"unimport ${describe(name)}"
    case Importee.Wildcard()           => "wildcard"
    case Importer(ref, importees)      => describe(ref) + " " + join(importees)
  }

  private def describeMisc(tree: Tree): String = tree match {
    case Case(pat, cond, body) => {
      mkString(
        "case",
        describePat(pat),
        cond.map(c => s"if ${describe(c)}").getOrElse(""),
        "then",
        describe(body)
      )
    }
    case Init(tpe, Name.Anonymous(), argss) => {
      val argssRes = curriedCall(argss)

      mkString(
        describe(tpe),
        argssRes
      )
    }
    case Pkg(ref, stats) =>
      mkString(
        "package",
        describe(ref),
        join(stats)
      )

    case Pkg.Object(mods, name, templ) =>
      mkString(
        join(mods),
        "package object",
        describe(name),
        describe(templ)
      )

    case Self(Name.Anonymous(), None) => ""
    case Self(name, decltpe) => {
      mkString(
        "self type",
        describe(name),
        decltpe.map(d => "typed as: " + describe(d)).getOrElse("")
      )
    }
    case Source(stats) => join(stats)
    case t @ Template(early, inits, self, stats) => {

      val isTermNewAnon = t.parent.exists(_.is[Term.NewAnonymous])

      val earlyRes =
        if (early.nonEmpty) "early initializer: " + join(early)
        else ""

      val initsRes =
        inits match {
          case Nil => ""
          case h :: t => {
            val keyword =
              if (early.isEmpty) {
                if (isTermNewAnon) ""
                else "extends "
              } else "with "

            mkString(
              keyword + describe(h),
              t.map(init => "with " + describe(init)).mkString(" ")
            )
          }
        }

      mkString(
        earlyRes,
        initsRes,
        describe(self),
        join(stats)
      )
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

      val nameRes =
        if (name.is[Name.Anonymous]) "a parameter"
        else describeTree(name)

      mkString(
        join(mods),
        nameRes,
        describeTparams(tparams),
        describe(tbounds),
        vboundsRes,
        cboundsRes
      )
    }
    case Name.Indeterminate(value) => value
    case Name.Anonymous()          => ""

    case Ctor.Primary(mods, Name.Anonymous(), paramss) => {
      mkString(
        join(mods),
        joinParamss(paramss)
      )
    }
    case s @ Ctor.Secondary(mods, _, paramss, Init(_, _, argss), stats) => {
      val bodySep =
        if (stats.isEmpty) ""
        else ","

      val prim = "primary constructor"

      val primaryCall =
        argss match {
          case List(Nil) | Nil => s"call to $prim"
          case _               => mkString(prim, curriedCall(argss))
        }

      mkString(
        join(mods),
        "secondary constructor",
        joinParamss(paramss),
        primaryCall,
        bodySep,
        join(stats)
      )
    }
  }

  private def joinParts(prefix: String,
                        verb: String,
                        parts: List[Tree],
                        args: List[Tree],
                        isInterpolation: Boolean): String = {

    val prefix0 =
      if (isInterpolation) s"$prefix interpolation"
      else prefix

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

          acc + sep0 + verb + " " + describe(l) + sep + dr
      }

    s"$prefix0 $body"
  }

  private def interpolation(prefix: String,
                            verb: String,
                            parts: List[Tree],
                            args: List[Tree]): String =
    joinParts(prefix, verb, parts, args, isInterpolation = true)

  private def describeDef(mods: List[Mod],
                          name: Term.Name,
                          tparams: List[scala.meta.Type.Param],
                          paramss: List[List[Term.Param]],
                          decltpe: Option[scala.meta.Type],
                          body: Option[Term],
                          isMacro: Boolean = false): String = {

    val paramssRes = joinParamss(paramss)

    val decltpeRes =
      decltpe match {
        case Some(tpe @ Type.Name(name)) if name == "Unit" && tpe.tokens.isEmpty => ""
        case Some(tpe)                                                           => "returns: " + describe(tpe)
        case _                                                                   => ""
      }

    val bodyRes =
      body
        .map { b =>
          val macroKw = if (isMacro) "macro " else ""
          macroKw + "body: " + describe(b)
        }
        .getOrElse("")

    mkString(
      join(mods),
      "def",
      describeTree(name),
      describeTparams(tparams),
      paramssRes,
      decltpeRes,
      bodyRes
    )
  }

  private def curriedCall(argss: List[List[Term]]): String =
    if (argss.nonEmpty)
      "applied to " + argss.map(_.map(describe).mkString(", ")).mkString(" then ")
    else ""

  private def describeTparams(tparams: List[scala.meta.Type.Param]): String = {
    if (tparams.nonEmpty) tparams.map(describe).mkString("parameterized with: ", ", ", "")
    else ""
  }

  private def tuples(args: List[Tree]): String =
    s"tuple ${args.size} of ${join(args)}"

  private def join(args: List[Tree]): String =
    args.map(describe).mkString(", ")

  private def joinParamss(paramss: List[List[Term.Param]]): String =
    if (paramss.nonEmpty) paramss.flatMap(_.map(describe)).mkString(", ")
    else ""

  private def option(opt: Option[Tree]): String =
    opt.map(describe).getOrElse("")

  private def mkString(parts: String*): String = parts.filter(_.nonEmpty).mkString(" ")
}
