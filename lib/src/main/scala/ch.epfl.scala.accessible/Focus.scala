package ch.epfl.scala.accessible

import scala.meta._
import java.nio.file.Path

object Focus {

  private def shortName(tree: Tree): String = {
    val full = tree.getClass.toString
    val lastDollard = full.lastIndexOf("$")
    full.slice(lastDollard + 1, full.size - "Impl".size)
  }

  def apply(path: Path, offset: Offset): Focus = {
    val tree = parse(path)
    apply(tree, offset)
  }

  def apply(tree: Tree, offset: Offset): Focus = {
    findPath(tree, tree => tree.pos.start <= offset.value && offset.value <= tree.pos.end)
  }

  def findPath(from: Tree, to: Tree): Focus = 
    findPath(from, tree => to.pos.start <= tree.pos.start && tree.pos.end <= to.pos.end)

  def findPath(from: Tree, within: Tree => Boolean): Focus = {
    val path = List.newBuilder[(Tree, Int)]
    var parent = from
    object navigate extends Traverser {
      override def apply(tree: Tree): Unit = {
        if (within(tree)) {
          val children = getChildren(parent)
          val childIndex = children.indexWhere(within)
          if(childIndex != -1) {
            if (tree != parent) {
              path += ((parent, childIndex))
              parent = tree
            }

            super.apply(tree)
          }
        }
      }
    }
    navigate(from)
    new Focus(root = from, path = path.result.reverse)
  }

  def apply(tree: Tree): Focus = {
    new Focus(root = tree, path = Nil)
  }

  private def getChildren(tree: Tree): Vector[Tree] = {
    tree.children.filter(_.tokens.nonEmpty).toVector
  }

  private def getChildren(parent: Tree, tree: Tree): (Boolean, Vector[Tree]) = {
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
      case t: Pkg         => (true, t.stats.toVector)

      case t: Defn.Var    => t.rhs.map(rhs => (true, Vector(rhs))).getOrElse(default)
      case _              => default
    }
  }
}

case class Focus private (root: Tree, path: List[(Tree, Int)]) {
  override def toString: String = showPath(path)

  private def showPath(path0: List[(Tree, Int)]): String = 
    path.map{ case (tree, child) => s"${Focus.shortName(tree)} $child" }.toString

  private def toRange(pos: Position): Range = Range(pos.start, pos.end)

  def current: Range = toRange(currentTree.pos)

  def currentTree: Tree = {
    path match {
      case Nil => root
      case (tree, child) :: _ => Focus.getChildren(tree)(child)
    }
  }

  def down: Focus = {
    def downWith(tree: Tree, children: Vector[Tree], getChild: Vector[Tree] => Tree): Focus = {
      val nextPath = 
        if (children.isEmpty) path
        else {
          val child = getChild(children)
          val childChildren = Focus.getChildren(child)

          if (childChildren.isEmpty) path
          else (child, 0) :: path
        }

      copy(path = nextPath)
    }

    path match {
      case Nil => 
        downWith(root, Focus.getChildren(root), _ => root)

      case (tree, childIndex) :: parentPath =>
        parentPath match {
          case (parent, _) :: _ =>
            val (isShorcut, children) = Focus.getChildren(parent, tree)
            
            if (!isShorcut) {
              downWith(tree, children, children => children(childIndex))
            } else {
              if (children.nonEmpty) {
                val child = children(0)
                val focus = Focus.findPath(tree, child)
                copy(path = focus.path ::: parentPath)
              } else {
                this
              }
            }

          case Nil => 
            downWith(tree, Focus.getChildren(tree), children => children(childIndex))
        }
    }
  }

  def up: Focus = {
    if(path.isEmpty) this
    else copy(path = path.tail)
  }

  def right: Focus = {
    path match {
      case Nil => this
      case (tree, childIndex) :: parentPath => {
        val children = Focus.getChildren(tree)
        val nextChildIndex = childIndex + 1
        if (nextChildIndex <= children.size - 1) {
          copy(path = (tree, nextChildIndex) :: parentPath)
        } else this
      }
    } 
  }

  def left: Focus = {
    path match {
      case Nil => this
      case (tree, childIndex) :: parentPath => {
        val nextChildIndex = childIndex - 1
        if (nextChildIndex >= 0) copy(path = (tree, nextChildIndex) :: parentPath)
        else this
      }
    } 
  }
}
