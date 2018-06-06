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

  def isRecursive(tree: Tree): Boolean = Recursive.unapply(tree).nonEmpty

  object Recursive {
    def unapply(tree: Tree): Option[Tree] = tree match {
      case Term.Select(Term.Select(qual, _), _)                    => Some(qual)
      case Term.Select(Term.Apply(fun, _), _)                      => Some(fun)
      case Term.Apply(Term.Select(qual, _), _)                     => Some(qual)
      case Term.ApplyInfix(Term.ApplyInfix(lhs, _, _, _), _, _, _) => Some(lhs)
      case _                                                       => None
    }
  }

  private def getChildrenRecursive(tree: Tree): Vector[Tree] = {
    tree match {
      case Recursive(t)                    => getChildrenRecursive(t)
      case Term.Select(qual: Term.Name, _) => Vector(qual)
      case _                               => Vector(tree)
    }
  }

  object RecursiveReverse {
    def unapply(tree: Tree): Option[Tree] = tree match {
      case Term.Select(Term.Select(_, name), _) => Some(name)
      case Term.Select(Term.Apply(fun, args), _) =>
        if (args.nonEmpty) Some(args.last)
        else Some(fun)
      case Term.Apply(Term.Select(_, name), args) =>
        if (args.nonEmpty) Some(args.last)
        else Some(name)
      case Term.ApplyInfix(Term.ApplyInfix(_, _, _, args), _, _, _) => Some(args.last)
      case Term.Select(_, sel)                                      => Some(sel)
      case Term.ApplyInfix(_, _, _, args)                           => Some(args.last)
      case _                                                        => None
    }
  }

  private def getChildrenRecursiveReverse(tree: Tree): Vector[Tree] = {
    tree match {
      case RecursiveReverse(t)             => getChildrenRecursiveReverse(t)
      case Term.Select(qual: Term.Name, _) => Vector(qual)
      case _                               => Vector(tree)
    }
  }

  def getChildren(tree: Tree, isDown: Boolean, isLeft: Boolean): Vector[Tree] = {
    tree match {
      case Recursive(t) if isDown => getChildrenRecursive(t)
      case _ if isLeft => {
        val children = tree.children.toList
        (children match {
          case RecursiveReverse(h) :: t => h :: t
          case _                        => children
        }).toVector
      }
      case _ => tree.children.filter(_.tokens.nonEmpty).toVector
    }
  }

  def getChildren(tree: Tree, parent: Tree): Vector[Tree] = {
    def default = getChildren(tree, isDown = true, isLeft = false)
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

    def loop(cursor: Cursor): (Int, String) = {
      cursor match {
        case Root(tree) => (0, tree.productPrefix)
        case Child(tree, parent) => {
          val (level, res) = loop(parent)
          val currentLevel = level + 1
          val levelIndent = indent * currentLevel
          (
            currentLevel,
            res + nl +
              levelIndent + tree.productPrefix + " " + ((tree.pos.start, tree.pos.end))
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
    val children = Cursor.getChildren(tree, parent.tree)
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
          // keep going if you are in a recursive tree, ex: a.b.c
          case Child(_, parent0) if (Cursor.isRecursive(parent0.tree)) => parent.right
          case _                                                       => this
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
      else this
    }
  }

  def up: Cursor = {
    if (tree.tokens.size == parent.tree.tokens.size) parent.up
    else parent
  }
}
