package ch.epfl.scala.accessible

object CursorPlayground extends CursorTestsUtils {
  test("playground") {
    doFocus(
      "class A { →a.b.c← }",
      ("class A { →a←.b.c }", down)
    )

    doFocus(
      "class A { →a.b.c← }",
      ("class A { →a←.b.c }", down)
    )
  }
}