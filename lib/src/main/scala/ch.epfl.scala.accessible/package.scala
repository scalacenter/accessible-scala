package ch.epfl.scala

import scala.meta._
import org.typelevel.paiges._
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

  type Tree2 = Tree with scala.Product
  def pretty(tree: Tree2, showFields: Boolean = false): String = {
    def prettyFromProduct(tree: Tree2): Doc = {
      val args = 
        tree.productFields.zip(tree.productIterator.toList).map{ case (k, v) =>
          val rhs = 
            v match {
              case t: Tree2 => pretty0(t)
              case _ => Doc.text(v.toString)
            }

          if (showFields) Doc.text(k) + Doc.text(" = ") + rhs
          else rhs
        }

      val prefix = Doc.text(tree.productPrefix) + Doc.char('(')
      val body = Doc.intercalate(Doc.char(',') + Doc.line, args)
      val suffix = Doc.char(')')
      body.tightBracketBy(prefix, suffix)
    }

    def pretty0(tree: Tree2, showField: Boolean = false): Doc = {
      tree match {
        case _: Term.Name => Doc.text(tree.structure)
        case _ => prettyFromProduct(tree)
      }
    }

    pretty0(tree).render(1)
  }
}