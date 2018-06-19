package ch.epfl.scala

import scala.meta._

package object accessible {

  def find(tree: Tree, offset: Offset): Option[Tree] = {
    var found: Option[Tree] = None
    object findPos extends Traverser {
      override def apply(tree: Tree): Unit = {
        if (tree.pos.start <= offset.value &&
            offset.value <= tree.pos.end) {
          found = Some(tree)
          super.apply(tree)
        }
      }
    }
    findPos(tree)
    found
  }

  def find(tree: Tree, range: Range): Option[Tree] = {
    var found: Option[Tree] = None
    object findPos extends Traverser {
      override def apply(tree: Tree): Unit = {
        if (tree.pos.start == range.start &&
            tree.pos.end == range.end) {
          found = Some(tree)
          super.apply(tree)
        }
      }
    }
    findPos(tree)
    found
  }
}
