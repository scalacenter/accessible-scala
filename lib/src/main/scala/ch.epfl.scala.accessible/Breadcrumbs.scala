package ch.epfl.scala.accessible

import scala.meta._

object Breadcrumbs {
  def apply(tree: Tree, offset: Offset): String = {
    val visited = List.newBuilder[Tree]
    object visit extends Traverser {
      override def apply(tree: Tree): Unit = {
        if (tree.pos.start <= offset.value &&
            offset.value <= tree.pos.end) {
          visited += tree
          super.apply(tree)
        }
      }
    }
    visit(tree)
    val trees = visited.result()
    trees.map(Summary.visitDefiniton).filterNot(_.isEmpty).mkString(", ")
  }
}