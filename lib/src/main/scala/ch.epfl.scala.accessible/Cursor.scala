package ch.epfl.scala.accessible

import scala.meta._

object Cursor {
  def apply(tree: Tree): Cursor = Root(tree)

  def apply(tree: Tree, offset: Offset): Cursor =
    buildCursor(Root(tree), tree => tree.pos.start <= offset.value && offset.value <= tree.pos.end)

  def apply(from: Cursor, to: Tree): Cursor = 
    buildCursor(from, tree => to.pos.start <= tree.pos.start && tree.pos.end <= to.pos.end)

  private def buildCursor(root: Cursor, within: Tree => Boolean): Cursor = {
    var cursor: Cursor = root
    object navigate extends Traverser {
      override def apply(tree: Tree): Unit = {
        if (within(tree)) {
          if (tree != root) {
            cursor = Child(tree, cursor)
          }
          super.apply(tree)
        }
      }
    }
    navigate(root.tree)
    cursor
  }

  def getChildren(tree: Tree): Vector[Tree] = {
    tree.children.filter(_.tokens.nonEmpty).toVector
  }

  def getChildren(tree: Tree, parent: Tree): (Boolean, Vector[Tree]) = {
    def default = (false, getChildren(tree))

    parent match {
      case t: Defn.Def    => (true, Vector(t.body))
      case t: Defn.Macro  => (true, Vector(t.body))
      case t: Defn.Object => (true, t.templ.stats.toVector)
      case t: Pkg.Object  => (true, t.templ.stats.toVector)
      case t: Defn.Val    => (true, Vector(t.rhs))
      case t: Defn.Class  => (true, t.templ.stats.toVector)
      case t: Defn.Trait  => (true, t.templ.stats.toVector)
      case t: Defn.Type   => (true, Vector(t.body))
      case t: Pkg         => (true, Vector(t.ref))
      case t: Defn.Var    => t.rhs.map(rhs => (true, Vector(rhs))).getOrElse(default)
      case _              => default
    }
  }
}

sealed trait Cursor {
  private def toRange(pos: Position): Range = Range(pos.start, pos.end)
  def current: Range = toRange(currentTree.pos)
  def currentTree: Tree
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
              levelIndent + shortName(tree)
          )
        }
      }
    }
    val (_, res) = loop(this)
    res
  }
}
case class Root private(val tree: Tree) extends Cursor {
  def down: Cursor = {
    val children = Cursor.getChildren(tree)

    if (children.nonEmpty) Child(children.head, this)
    else this
  }
  def right: Cursor = this
  def left: Cursor = this
  def up: Cursor = this
  def currentTree: Tree = tree
}
case class Child private(val tree: Tree, parent: Cursor) extends Cursor {
  def down: Cursor = {
    val (isShortcut, children) = Cursor.getChildren(tree, parent.tree)
    if (children.nonEmpty) {
      if (!isShortcut) Child(children.head, this)
      else Cursor(this, children.head)
    }
    else this
  }
  def right: Cursor = {
    val children = Cursor.getChildren(parent.tree)
    val idx = children.indexWhere(_ == tree)
    if (idx < children.size - 1) Child(children(idx + 1), parent)
    else this
  }
  def left: Cursor = {
    val children = Cursor.getChildren(parent.tree)
    val idx = children.indexWhere(_ == tree)
    if (idx > 0) Child(children(idx - 1), parent)
    else this
  }
  def up: Cursor = parent
  def currentTree: Tree = tree
}

