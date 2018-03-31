package ch.epfl.scala.accessible

import scala.meta._
import scala.meta.testkit.DiffAssertions
import org.scalameta.logger

object SummaryDescriptionTests extends FunSuite with DiffAssertions {
  check(
    """|// --
       |object A {
       |  val a = 1
       |  def f = 1
       |  trait A { def f = 1 }
       |  class B { def f = 1 }
       |  object C { def f = 1 }
       |}""".stripMargin,
    """|object A:
       |val a,
       |def f,
       |trait A,
       |class B,
       |object C.""".stripMargin
  )

  // we can visit childrens:
  check("object A { trait B }", "object A: trait B.")
  check("class A { trait B }", "class A: trait B.")
  check("package a { trait B }", "package a: trait B.")
  check("package object a { trait B }", "package object a: trait B.")

  def check(source: String, expected: String): Unit = {
    val testName = logger.revealWhitespace(source)
    test(testName) {
      val tree = source.parse[Source].get
      val pos = tree.stats.head.pos.start
      val obtained = Summary(tree, Some(Offset(pos)))
      assertNoDiff(obtained, expected)
    }
  }
}
