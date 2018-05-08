package ch.epfl.scala.accessible

// import espeak.Espeak

import scala.meta._
import scala.meta.testkit.DiffAssertions
import org.scalameta.logger
import scala.meta.parsers.Parse

object DescribeTests extends FunSuite with DiffAssertions {
  // System.loadLibrary("scala-espeak0")
  // val espeak = new Espeak

  // check(
  //   "trait Monad[F[_]] extends Applicative[F]",
  //   "trait Monad with higher type F extending applicative of F"
  // )

  checkType("B", "B") // Type.Name
  // checkType("(A => B)", "function: A to B")
  // checkType("(() => B)", "function: Unit to B")
  // checkType("((A, B) => C)", "function: A, B to C")
  // checkType("(A, B)", "tuple 2: A, B")
  // checkType("F[T]")                 // Type.Apply

  // checkType("a.B", "")                  // Type.Select
  // checkType("a#B")                  // Type.Project
  // checkType("this.type")            // Type.Singleton
  // checkType("t.type")               // Type.Singleton
  // checkType("K Map V")              // Type.ApplyInfix
  // checkType("implicit A => B")      // Type.ImplicitFunction
  // checkType("A with B")             // Type.With2
  // checkType("A & B")                // Type.And
  // checkType("A | B", dotty)         // Type.Or
  // checkType("A { def f: B }")       // Type.Refine
  // checkType("A{}")                  // Type.Refine
  // checkType("{ def f: B }")         // Type.Refine
  // checkType("A forSome { type T }") // Type.Existential
  // checkType("T @A")                 // Type.Annotate
  // checkType("[X] => (X, X)")        // Type.Lambda
  // checkType("_")                    // Type.Placeholder
  // check("def f[T >: A <: B]", "def f type param T upper bound by A and lower bound by B")
  // checkType("T <: B", "T lower bound by B")
  // checkType("T >: A", "T upper bound by A")
  // check("def f[A <% B]: C", "def f parametrized with A view bound by B returns C")
  // check("def f[A: B]: C", "def f parametrized with A context bound by B returns C")
  // check("def f[A : B : C]: D", "def f parametrized with A context bound by B and C returns D")
  // checkType("=> T", "T by name") // Type.ByName
  // checkType("Any*")                 // Type.Repeated
  // check("trait A[X]")               // Type.Param
  // check("def f[@a A]: B")           // Type.Param (annotations)
  // checkPat("List[t](xs @ _*)")      // Type.Var

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
      // espeak.synthesize(obtained)
      // espeak.synchronize()
    }
  }
}
