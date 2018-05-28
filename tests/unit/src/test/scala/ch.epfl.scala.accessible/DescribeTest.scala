package ch.epfl.scala.accessible

import scala.meta._
import scala.meta.testkit.DiffAssertions
import org.scalameta.logger
import scala.meta.parsers.Parse

object DescribeTests extends FunSuite with DiffAssertions {

  // checkType("B", "B")
  check(
    "class A[_ <: M[_, _]]",
    "class A parametrized with: upper bounded by: M taking 2 parameters"
  )

  def check(source: String, expected: String): Unit =
    check(source, expected, Parse.parseStat)

  def checkType(source: String, expected: String): Unit =
    check(source, expected, Parse.parseType)

  def check(source: String, expected: String, parser: Parse[_ <: Tree]): Unit = {
    val testName = logger.revealWhitespace(source)
    test(testName) {
      val tree = parser.apply(Input.String(source), dialects.Scala212).get
      val obtained = Describe(tree, Offset(0))
      assertNoDiff(obtained, expected)
    }
  }
}
