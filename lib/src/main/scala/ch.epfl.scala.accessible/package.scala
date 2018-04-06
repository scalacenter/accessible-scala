package ch.epfl.scala

import scala.meta._
import java.nio.file.{Files, Path}
import java.nio.charset.StandardCharsets

package object accessible {
  def parse(path: Path): Tree = {
    val text = new String(Files.readAllBytes(path), StandardCharsets.UTF_8)
    val input = Input.String(text)
    input.parse[Source].get
  }

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
}