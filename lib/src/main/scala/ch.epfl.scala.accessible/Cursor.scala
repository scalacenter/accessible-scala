package ch.epfl.scala.accessible

import scala.meta._
import java.nio.file.Path

object Cursor {
  def apply(tree: Tree): Cursor =
    Root(tree)

  def apply(path: Path, range: Range): Cursor = {
    val tree = parse(path)
    apply(tree, range)
  }

  def apply(tree: Tree, range: Range): Cursor = {
    buildCursor(Root(tree), tree => tree.pos.start <= range.start && range.end <= tree.pos.end)
  }

  def apply(from: Cursor, to: Tree): Cursor = {
    buildCursor(from, tree => tree.pos.start <= to.pos.start && to.pos.end <= tree.pos.end)
  }

  private def buildCursor(root: Cursor, within: Tree => Boolean): Cursor = {
    var cursor: Cursor = root
    object navigate extends Traverser {
      override def apply(tree: Tree): Unit = {
        if (within(tree)) {
          if (tree != root.tree) {
            cursor = Child(tree, cursor)
          }
          super.apply(tree)
        }
      }
    }
    navigate(root.tree)
    cursor
  }

  def isSelectChain(tree: Tree): Boolean = SelectChain.unapply(tree).nonEmpty

  object SelectChain {
    def unapply(tree: Tree): Option[Tree] = tree match {
      case Term.Select(Term.Select(qual, _), _) => Some(qual)
      case Term.Select(Term.Apply(fun, _), _)   => Some(fun)
      case Term.Apply(Term.Select(qual, _), _)  => Some(qual)
      case _                                    => None
    }
  }

  object SelectChainReverse {
    def unapply(tree: Tree): Option[Tree] = tree match {
      case Term.Select(Term.Select(_, name), _) => Some(name)
      case Term.Select(Term.Apply(fun, args), _) =>
        if (args.nonEmpty) Some(args.last)
        else Some(fun)
      case Term.Apply(Term.Select(_, name), _) => Some(name)
      case _                                   => None
    }
  }

  private def getChildrenSelectChain(tree: Tree): Vector[Tree] = {
    tree match {
      case SelectChain(t)                  => getChildrenSelectChain(t)
      case Term.Select(qual: Term.Name, _) => Vector(qual)
      case _                               => Vector(tree)
    }
  }

  private def getChildrenSelectChainReverse(tree: Tree): Vector[Tree] = {
    tree match {
      case SelectChainReverse(t) => getChildrenSelectChainReverse(t)
      case Term.Select(_, name)  => Vector(name)
      case _                     => Vector(tree)
    }
  }

  def getChildren(tree: Tree, isDown: Boolean, isLeft: Boolean): Vector[Tree] = {
    tree match {
      case SelectChain(qual) if isDown        => getChildrenSelectChain(qual)
      case SelectChainReverse(qual) if isLeft => getChildrenSelectChainReverse(qual)
      case _                                  => tree.children.filter(_.tokens.nonEmpty).toVector
    }

  }

  def getChildren(tree: Tree, parent: Tree, isDown: Boolean): Vector[Tree] = {
    def default = getChildren(tree, isDown, isLeft = false)

    (tree, parent) match {
      case (_, t: Defn.Def)              => Vector(t.body)
      case (_, t: Defn.Macro)            => Vector(t.body)
      case (_, t: Defn.Object)           => t.templ.stats.toVector
      case (_, t: Pkg.Object)            => t.templ.stats.toVector
      case (_, t: Defn.Val)              => Vector(t.rhs)
      case (_: Type.Name, t: Defn.Class) => t.templ.stats.toVector
      case (_, t: Defn.Trait)            => t.templ.stats.toVector
      case (_, t: Defn.Type)             => Vector(t.body)
      case (_, t: Defn.Var)              => t.rhs.map(rhs => Vector(rhs)).getOrElse(default)
      case _                             => default
    }
  }
}

sealed trait Cursor {
  def current: Range = toRange(tree.pos)
  def tree: Tree

  def up: Cursor
  def down: Cursor
  def left: Cursor
  def right: Cursor

  override def toString: String = {
    val indent = "  "
    val nl = '\n'

    def shortName(tree: Tree): String = {
      val full = tree.getClass.toString
      val lastDollard = full.lastIndexOf("$")
      full.slice(lastDollard + 1, full.size - "Impl".size)
    }

    def loop(cursor: Cursor): (Int, String) = {
      cursor match {
        case Root(tree) => (0, shortName(tree))
        case Child(tree, parent) => {
          val (level, res) = loop(parent)
          val currentLevel = level + 1
          val levelIndent = indent * currentLevel
          (
            currentLevel,
            res + nl +
              levelIndent + shortName(tree) // + " " +((tree.pos.start, tree.pos.end))
          )
        }
      }
    }
    val (_, res) = loop(this)
    res
  }
  private def toRange(pos: Position): Range = Range(pos.start, pos.end)
}
case class Root private (val tree: Tree) extends Cursor {
  def down: Cursor = {
    val children = Cursor.getChildren(tree, isDown = true, isLeft = false)
    if (children.nonEmpty) Child(children.head, this)
    else this
  }
  def right: Cursor = this
  def left: Cursor = this
  def up: Cursor = this
}
case class Child private (val tree: Tree, parent: Cursor) extends Cursor {

  def down: Cursor = {
    val children = Cursor.getChildren(tree, parent.tree, isDown = true)
    if (children.nonEmpty) Child(children.head, this)
    else this
  }

  def right: Cursor = {
    val children = Cursor.getChildren(parent.tree, isDown = false, isLeft = false)
    val idx = children.indexWhere(_ == tree)
    if (idx < children.size - 1) Child(children(idx + 1), parent)
    else {
      if (tree.tokens.size == parent.tree.tokens.size) {
        // when you select by it's offset, it will go as deep as possible
        // for example:
        // val →x← = 1
        // Defn.Val(Nil, List(Pat.Var(→Term.Name("a")←)), None, Lit.Int(1))
        parent.right
      } else {
        parent match {
          // keep going if you are in select chain, ex: a.b.c
          case Child(_, parent0) if (Cursor.isSelectChain(parent0.tree)) => parent.right
          case _                                                         => this
        }
      }
    }
  }

  def left: Cursor = {
    val children = Cursor.getChildren(parent.tree, isDown = false, isLeft = true)
    val idx = children.indexWhere(_ == tree)
    if (idx > 0) Child(children(idx - 1), parent)
    else {
      if (tree.tokens.size == parent.tree.tokens.size) parent.left
      else {
        // navigate left on a select chain.
        // ex: foo.bar(arg).→buzz← to foo.bar(→arg←).buzz
        if (children.size == 1) Cursor(parent, children.head)
        else this
      }
    }
  }

  def up: Cursor = {
    if (tree.tokens.size == parent.tree.tokens.size) parent.up
    else parent
  }
}
