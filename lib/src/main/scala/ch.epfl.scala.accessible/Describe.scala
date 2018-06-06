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

  def describe(tree: Tree): String = tree match {
    case lit: Lit                              => describe(lit)
    case term: Term                            => describe(term)
    case tpe: Type                             => describe(tpe)
    case pat: Pat                              => describe(pat)
    case decl: Decl                            => describe(decl)
    case defn: Defn                            => describe(defn)
    case mod: Mod                              => describe(mod)
    case enum: Enumerator                      => describe(enum)
    case ctor: Ctor                            => describe(ctor)
    case _: Import | _: Importer | _: Importee => describeImports(tree)
    case _ => describeMisc(tree)
  }

  def describe(lit: Lit): String = lit.syntax

  def describe(term: Term): String = term match {
    case This(qual)                             => "todo"
    case Super(thisp, superp)                   => "todo"
    case Name(value)                            => "todo"
    case Select(qual, name)                     => "todo"
    case Interpolate(prefix, parts, args)       => "todo"
    case Xml(parts, args)                       => "todo"
    case Apply(fun, args)                       => "todo"
    case ApplyType(fun, targs)                  => "todo"
    case ApplyInfix(lhs, op, targs, args)       => "todo"
    case Assign(lhs, rhs)                       => "todo"
    case Return(expr)                           => "todo"
    case Throw(expr)                            => "todo"
    case Ascribe(expr, tpe)                     => "todo"
    case Annotate(expr, annots)                 => "todo"
    case Tuple(args)                            => "todo"
    case Block(stats)                           => "todo"
    case If(cond, thenp, elsep)                 => "todo"
    case Match(expr, cases)                     => "todo"
    case Try(expr, catchp, finallyp)            => "todo"
    case TryWithHandler(expr, catchp, finallyp) => "todo"
    case Function(params, body)                 => "todo"
    case PartialFunction(cases)                 => "todo"
    case While(expr, body)                      => "todo"
    case Do(body, expr)                         => "todo"
    case For(enums, body)                       => "todo"
    case ForYield(enums, body)                  => "todo"
    case New(init)                              => "todo"
    case NewAnonymous(templ)                    => "todo"
    case Placeholder()                          => "todo"
    case Eta(expr)                              => "todo"
    case Repeated(expr)                         => "todo"
    case Param(mods, name, decltpe, default)    => "todo"
  }

  def describe(tpe: Type): String = tpe match {
    case Name(value)                                           => "todo"
    case Select(qual, name)                                    => "todo"
    case Project(qual, name)                                   => "todo"
    case Singleton(ref)                                        => "todo"
    case Apply(tpe, args)                                      => "todo"
    case ApplyInfix(lhs, op, rhs)                              => "todo"
    case Function(params, res)                                 => "todo"
    case ImplicitFunction(params, res)                         => "todo"
    case Tuple(args)                                           => "todo"
    case With(lhs, rhs)                                        => "todo"
    case And(lhs, rhs)                                         => "todo"
    case Or(lhs, rhs)                                          => "todo"
    case Refine(tpe, stats)                                    => "todo"
    case Existential(tpe, stats)                               => "todo"
    case Annotate(tpe, annots)                                 => "todo"
    case Lambda(tparams, tpe)                                  => "todo"
    case Method(paramss, tpe)                                  => "todo"
    case Placeholder(bounds)                                   => "todo"
    case Bounds(lo, hi)                                        => "todo"
    case ByName(tpe)                                           => "todo"
    case Repeated(tpe)                                         => "todo"
    case Var(name)                                             => "todo"
    case Param(mods, name, tparams, tbounds, vbounds, cbounds) => "todo"
  }

  def describe(pat: Pat): String = pat match {
    case Var(name)                        => "todo"
    case Wildcard()                       => "todo"
    case SeqWildcard()                    => "todo"
    case Bind(lhs, rhs)                   => "todo"
    case Alternative(lhs, rhs)            => "todo"
    case Tuple(args)                      => "todo"
    case Extract(fun, args)               => "todo"
    case ExtractInfix(lhs, op, rhs)       => "todo"
    case Interpolate(prefix, parts, args) => "todo"
    case Xml(parts, args)                 => "todo"
    case Typed(lhs, rhs)                  => "todo"
    case _                                => "todo"
  }

  def describe(decl: Decl): String = decl match {
    case Val(mods, pats, decltpe)                   => "todo"
    case Var(mods, pats, decltpe)                   => "todo"
    case Def(mods, name, tparams, paramss, decltpe) => "todo"
    case Type(mods, name, tparams, bounds)          => "todo"
  }

  def describe(defn: Defn): String = defn match {
    case Val(mods, pats, decltpe, rhs)                      => "todo"
    case Var(mods, pats, decltpe, rhs)                      => "todo"
    case Def(mods, name, tparams, paramss, decltpe, body)   => "todo"
    case Macro(mods, name, tparams, paramss, decltpe, body) => "todo"
    case Type(mods, name, tparams, body)                    => "todo"
    case Class(mods, name, tparams, ctor, templ)            => "todo"
    case Trait(mods, name, tparams, ctor, templ)            => "todo"
    case Object(mods, name, templ)                          => "todo"
  }

  def describe(mod: Mod): String = {
    case Annot(init)       => "todo"
    case Private(within)   => "todo"
    case Protected(within) => "todo"
    case Covariant()       => "todo"
    case Contravariant()   => "todo"
    case Implicit()        => "todo"
    case Final()           => "todo"
    case Sealed()          => "todo"
    case Override()        => "todo"
    case Case()            => "todo"
    case Abstract()        => "todo"
    case Lazy()            => "todo"
    case ValParam()        => "todo"
    case VarParam()        => "todo"
    case Inline()          => "todo"
  }

  def describe(enum: Enumerator): String = enum match {
    case Generator(pat, rhs) => "todo"
    case Val(pat, rhs)       => "todo"
    case Guard(cond)         => "todo"
  }

  def describe(ctor: Ctor): String = ctor match {
    case Primary(mods, name, paramss)                => "todo"
    case Secondary(mods, name, paramss, init, stats) => "todo"
  }

  def describeImports(tree: Tree): String = tree match {
    case Import(importers)        => "todo"
    case Importer(ref, importees) => "todo"
    case Wildcard()               => "todo"
    case Name(name)               => "todo"
    case Rename(name, rename)     => "todo"
    case Unimport(name)           => "todo"
  }

  def describeMisc(tree: Tree): String = tree match {
    case Source(stats)                       => "todo"
    case Case(pat, cond, body)               => "todo"
    case Pkg(ref, stats)                     => "todo"
    case Pkg.Object(mods, name, templ)       => "todo"
    case Init(tpe, name, argss)              => "todo"
    case Self(name, decltpe)                 => "todo"
    case Template(early, inits, self, stats) => "todo"
  }
}
