package ch.epfl.scala.accessible

import scala.meta._
import munit.Assertions.assertNoDiff
import org.scalameta.logger

object BreadcrumbsTests extends FunSuite {
  check(
    """|package a
       |object b {
       |  class c {
       |    def m = {
       |      here
       |    }
       |  }
       |}""".stripMargin,
    """package a object b class c def m"""
  )

  def check(source: String, expected: String): Unit = {
    val testName = logger.revealWhitespace(source)
    test(testName) {
      val tree = source.parse[Source].get
      val offset = findHere(tree).get
      val obtained = Breadcrumbs(tree, offset)
      assertNoDiff(obtained, expected)
    }
  }

  private def findHere(tree: Tree): Option[Offset] = {
    var found: Option[Offset] = None
    object findPos extends Traverser {
      override def apply(tree: Tree): Unit = {
        tree match {
          case q"here" => found = Some(Offset(tree.pos.start))
          case _       => super.apply(tree)
        }
      }
    }
    findPos(tree)
    found
  }
}
