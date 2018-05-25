package ch.epfl.scala.accessible

object CursorPlayground extends CursorTestsUtils {
  test("playground") {
    doFocus(
      "class A→(a: Int, b: Int)←",
      ("class A(→a: Int←, b: Int)", down)
    )
  }
}