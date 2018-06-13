package ch.epfl.scala.accessible

import scala.meta._

object Playground extends DescribeTestsUtils {
  check[Lit](q"1", "1")
}
