package ch.epfl.scala.accessible

object CursorPlayground extends CursorTestsUtils {
  test("a") {
    doFocus(
      "class A { →test(a)← { } }",
      ("class A { →test(a) { }← }", up)
    )
  }
}