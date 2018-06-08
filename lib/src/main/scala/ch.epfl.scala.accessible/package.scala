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

  def wrapList(args: List[Doc]): Doc = {
    if (args.nonEmpty) wrap(Doc.text("List") + Doc.char('('), args, Doc.char(')'))
    else Doc.text("Nil")
  }

  def wrap(prefix: Doc, args: List[Doc], suffix: Doc): Doc = {
    val body = Doc.intercalate(Doc.char(',') + Doc.line, args)
    body.tightBracketBy(prefix, suffix)
  }

  def prettyDoc(tree: List[Tree], showFields: Boolean): Doc = {
    wrapList(tree.map(t => prettyDoc(t, showFields)))
  }

  def prettyDoc(tree: Tree, showFields: Boolean): Doc = {
    tree match {
      case _ if tree.tokens.isEmpty => Doc.empty
      case v: Term.Name             => Doc.text(v.structure)
      case t: Type.Name             => Doc.text(t.structure)
      case _ => {
        val args =
          tree.productFields.zip(tree.productIterator.toList).map {
            case (k, v) =>
              val rhs =
                v match {
                  case v: Term.Name => Doc.text(v.structure)
                  case t: Tree      => prettyDoc(t, showFields)
                  case o: Option[_] =>
                    o match {
                      case Some(t: Tree) =>
                        wrap(
                          Doc.text("Some") + Doc.char('('),
                          List(prettyDoc(t, showFields)),
                          Doc.char(')')
                        )
                      case None => Doc.text("None")
                      case _    => throw new Exception("cannot handle: " + o)
                    }
                  case vs: List[_] =>
                    vs match {
                      case Nil => Doc.text("Nil")
                      case (h: Tree) :: _ => {
                        prettyDoc(vs.asInstanceOf[List[Tree]], showFields)
                      }
                      case (h: List[_]) :: _ => {
                        val vsT = vs.asInstanceOf[List[List[Tree]]]
                        wrapList(vsT.map(v => prettyDoc(v, showFields)))
                      }
                      case _ => throw new Exception("cannot handle: " + vs)
                    }
                  case _ => Doc.text(v.toString)
                }

              if (showFields) Doc.text(k) + Doc.text(" = ") + rhs
              else rhs
          }

        wrap(Doc.text(tree.productPrefix) + Doc.char('('), args, Doc.char(')'))
      }
    }
  }

  def pretty(tree: Tree, showFields: Boolean = false): String = {
    prettyDoc(tree, showFields).render(1)
  }

  def pretty(tree: List[Tree], showFields: Boolean): String = {
    prettyDoc(tree, showFields).render(1)
  }
}
