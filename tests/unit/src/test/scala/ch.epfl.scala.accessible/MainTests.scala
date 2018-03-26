package ch.epfl.scala.accessible

import utest._

object MainTests extends TestSuite {
  val tests = Tests {
    'main - {
      Main.main(Array())
    }
  }
}
