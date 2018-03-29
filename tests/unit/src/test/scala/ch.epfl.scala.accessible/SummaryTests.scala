package ch.epfl.scala.accessible

import utest._
import scala.meta._

object SummaryTests extends TestSuite {

  val tests = Tests {
    "top level summary" - {
      check(
        """|package org.example
           |
           |import foo._
           |
           |package object A { def f = 1 }
           |object B { def f = 1 }
           |class C { def f = 1 }
           |trait D { def f = 1 }
           |
           |""".stripMargin,

        """|package org example.
           |package object A,
           |object B,
           |class C,
           |trait D.""".stripMargin
      )
    }

    "class summary" - {
      checkDefinition(
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
    }
  }

  def check(source: String, expected: String): Unit = {
    val tree = source.parse[Source].get
    val obtained = Summary(tree)
    assert(obtained == expected)
  }

  def checkDefinition(source: String, expected: String): Unit = {
    val tree = source.parse[Source].get
    val pos = tree.stats.head.pos
    val obtained = Summary(tree, Some(pos))
    assert(obtained == expected)
  }
}