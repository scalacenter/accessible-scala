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
    def within(tree: Tree): Boolean = 
      tree.pos.start <= offset.value && offset.value <= tree.pos.end

    object navigate extends Traverser {
      override def apply(tree: Tree): Unit = {
        if (within(tree)) {

        }
      }
    }
    navigate(tree)

    new Focus(root = tree, path = Nil)
  }

  def apply(tree: Tree): Focus = {
    new Focus(root = tree, path = Nil)
  }

  private def getChildren(tree: Tree): Vector[Tree] = {
    tree.children.filter(_.tokens.nonEmpty).toVector
  }

  // private def getChildren(parent: Tree, tree: Tree): Vector[Tree] = {
  //   def default = getChildrenDefault(tree)
  //   parent match {
  //     case t: Defn.Def    => Vector(t.body)
  //     case t: Defn.Macro  => Vector(t.body)
  //     case t: Defn.Object => t.templ.stats.toVector
  //     case t: Pkg.Object  => t.templ.stats.toVector
  //     case t: Defn.Val    => Vector(t.rhs)
  //     case t: Defn.Var    => t.rhs.map(Vector(_)).getOrElse(default)
  //     case t: Defn.Class  => t.templ.stats.toVector
  //     case t: Defn.Trait  => t.templ.stats.toVector
  //     case t: Defn.Type   => Vector(t.body)
  //     case t: Pkg         => t.stats.toVector
  //     case _              => default
  //   }
  // }
}

case class Focus private (root: Tree, path: List[(Tree, Int)]) {
  override def toString: String = showPath(path)

  private def showPath(path0: List[(Tree, Int)]): String = 
    path.map{ case (tree, child) => s"${Focus.shortName(tree)} $child" }.toString

  private def toRange(pos: Position): Range = Range(pos.start, pos.end)

  def current: Range = toRange(currentTree.pos)

  def currentTree: Tree = {

    // println(toString)

    try {
      path match {
        case Nil => root
        case (tree, child) :: _ => Focus.getChildren(tree)(child)
      }
    } catch {
      case e => 
        println()
        println(toString)
        throw e
    }
  }

  def down: Focus = {
    def downWith(tree: Tree, getChildren: Vector[Tree] => Tree): Focus = {
      val children = Focus.getChildren(tree)

      val nextPath = 
        if (children.isEmpty) path
        else (getChildren(children), 0) :: path

      println()
      println(Focus.shortName(tree))
      println(children.map(Focus.shortName))
      println(showPath(nextPath))

      copy(path = nextPath)
    }

    path match {
      case Nil => downWith(root, _ => root)
      case (tree, childIndex) :: _ => downWith(tree, children => children(childIndex))
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
