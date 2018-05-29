package ch.epfl.scala.accessible

import scala.meta._
import scala.meta.testkit.DiffAssertions
import org.scalameta.logger
import scala.meta.parsers.Parse

object DescribeTests extends FunSuite with DiffAssertions {

  check("class A[_ <: M[_, _]]",
        "class A parametrized with: a parameter sub-type of: M taking 2 parameters")

  check("def f[T >: A <: B]",
        "def f parametrized with: T super-type of: A, sub-type of: B.\nreturns: Unit")

  check("def sum(args: Int*): Int", "def sum args repeated: Int.\nreturns: Int")

  check("def unapply[H, T <: HList](l: HCons[H, T]) = foo",
        "def unapply parametrized with: H, T sub-type of: HList l HCons of H, T.\nbody: foo")

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
