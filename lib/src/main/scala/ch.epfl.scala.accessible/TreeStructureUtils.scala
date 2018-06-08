package ch.epfl.scala.accessible

import scala.meta._
import org.typelevel.paiges._

import scala.language.implicitConversions

trait TreeStructureUtils {
  def pretty(input: CanPretty, showFields: Boolean = false): String = {
    input match {
      case TreeCanPretty(tree)       => prettyTree(tree, showFields)
      case ListTreeCanPretty(tree)   => prettyList(tree, showFields)
      case OptionTreeCanPretty(tree) => prettyOption(tree, showFields)
    }
  }

  // workarround to for polymorphic methods with default parameter
  sealed trait CanPretty
  case class TreeCanPretty(tree: Tree) extends CanPretty
  case class ListTreeCanPretty(tree: List[Tree]) extends CanPretty
  case class OptionTreeCanPretty(tree: Option[Tree]) extends CanPretty

  implicit def treeCanPretty(tree: Tree): CanPretty = TreeCanPretty(tree)
  implicit def listTreeCanPretty(tree: List[Tree]): CanPretty = ListTreeCanPretty(tree)
  implicit def optionTreeCanPretty(tree: Option[Tree]): CanPretty = OptionTreeCanPretty(tree)

  private def prettyTree(tree: Tree, showFields: Boolean): String = {
    prettyDoc(tree, showFields).render(1)
  }

  private def prettyList(tree: List[Tree], showFields: Boolean): String = {
    prettyDoc(tree, showFields).render(1)
  }

  private def prettyOption(tree: Option[Tree], showFields: Boolean): String = {
    prettyDoc(tree, showFields).render(1)
  }

  private def prettyDoc(tree: List[Tree], showFields: Boolean): Doc = {
    wrapList(tree.map(t => prettyDoc(t, showFields)))
  }

  private def prettyDoc(tree: Option[Tree], showFields: Boolean): Doc = {
    wrapOption(tree.map(t => prettyDoc(t, showFields)))
  }

  private def prettyDoc(tree: Tree, showFields: Boolean): Doc = {
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

  private def wrapList(args: List[Doc]): Doc = {
    if (args.nonEmpty) wrap(Doc.text("List") + Doc.char('('), args, Doc.char(')'))
    else Doc.text("Nil")
  }

  private def wrapOption(opt: Option[Doc]): Doc = {
    opt match {
      case Some(doc) => Doc.text("Some") + Doc.char('(') + doc + Doc.char(')')
      case None      => Doc.text("None")
    }
  }

  private def wrap(prefix: Doc, args: List[Doc], suffix: Doc): Doc = {
    val body = Doc.intercalate(Doc.char(',') + Doc.line, args)
    body.tightBracketBy(prefix, suffix)
  }
}
