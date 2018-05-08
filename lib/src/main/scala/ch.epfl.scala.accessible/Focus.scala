package ch.epfl.scala.accessible

import scala.meta._

object Focus {
  def apply(tree: Tree): Focus = {
    new Focus(parents = List(tree), children = Vector(tree), child = List(0))
  }
}

case class Focus private (var parents: List[Tree],
                          var children: Vector[Tree],
                          var child: List[Int]) {

  private def toPos(pos: Position): Range = Range(pos.start, pos.end)

  def current: Range = toPos(currentTree.pos)
  def currentTree: Tree = children(child.head)

  def down(): Focus = {
    val currentParent = children(child.head)
    val newChildrens = getChildren(currentParent)
    if (newChildrens.nonEmpty) {
      parents = currentParent :: parents
      child = 0 :: child
      children = newChildrens
    }
    this
  }
  private def showPos(tree: Tree): (Int, Int) = {
    (tree.pos.start, tree.pos.end)
  }
  def up(): Focus = {
    if (parents.size > 1) {
      parents = parents.tail
      val currentParent = parents.head
      child = child.tail
      children = getChildren(currentParent)
    }
    this
  }
  private def getChildren(tree: Tree): Vector[Tree] = {
    def default = childrens(tree)

    def childrens(t: Tree): Vector[Tree] =
      t.children.toVector.filter(_.tokens.nonEmpty)

    val parent = parents.head

    parent match {
      case t: Defn.Def    => Vector(t.body)
      case t: Defn.Macro  => Vector(t.body)
      case t: Defn.Object => t.templ.stats.toVector
      case t: Pkg.Object  => t.templ.stats.toVector
      case t: Defn.Val    => Vector(t.rhs)
      case t: Defn.Var    => t.rhs.map(Vector(_)).getOrElse(default)
      case t: Defn.Class  => t.templ.stats.toVector
      case t: Defn.Trait  => t.templ.stats.toVector
      case t: Defn.Type   => Vector(t.body)
      case t: Pkg         => t.stats.toVector
      case _              => default
    }
  }

  def left(): Focus = {
    if (child.head > 0) {
      child = (child.head - 1) :: child.tail
    }
    this
  }
  def right(): Focus = {
    if (child.head < children.size - 1) {
      child = (child.head + 1) :: child.tail
    }
    this
  }
}
