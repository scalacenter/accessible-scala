package ch.epfl.scala.accessible

import scala.meta._

object DescribeTests extends DescribeTestsUtils {
  // Lit
  // check[Lit]("1")

  // Term

  // Type
  check[Type.Name]("B", "B")

  // Mod
  check[Mod.Abstract]("abstract class A", "abstract class A")
  // check[Mod.Annot]("@tailrec def f = 1", "at tailrec def f = 1")
  check[Mod.Contravariant]("class A[→-T←]", "contra-variant T")
  check[Mod.Covariant]("class A[→+T←]", "co-variant T")
}
