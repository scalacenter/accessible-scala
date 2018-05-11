package ch.epfl.scala.accessible

object CursorSuite extends CursorTestsUtils {
  test("current") {
    doFocus(
      "class A; class B",
      ("→class A; class B←", noop)
    )
  }

  test("down") {
    doFocus(
      "class A; class B",
      ("→class A←; class B", down)
    )
  }

  test("down") {
    doFocus(
      "package a.b",
      ("package →a.b←", down),
      ("package →a←.b", down)
    )
  }

  test("right") {
    doFocus(
      "class A; class B",
      ("→class A←; class B", down),
      ("class A; →class B←", right),
      ("class A; →class B←", right)
    )
  }

  test("right") {
    doFocus(
      "object A { val →x← = 1}",
      ("object A { val x = →1←}", right)
    )
  }

  test("left") {
    doFocus(
      "class A; class B",
      ("→class A←; class B", down),
      ("class A; →class B←", right),
      ("→class A←; class B", left)
    )
  }

  test("preserve children position") {
    doFocus(
      "class A; class B { val c = 1 }",
      ("→class A←; class B { val c = 1 }", down),
      ("class A; →class B { val c = 1 }←", right),
      ("class A; class →B← { val c = 1 }", down),
      ("class A; →class B { val c = 1 }←", up)
    )
  }

  test("no children") {
    doFocus(
      "",
      ("→←", down)
    )
  }

  test("no children (bis)") {
    doFocus(
      "class A",
      ("class →A←", down),
      ("class →A←", down)
    )
  }

  test("Shorcut: class name to stat") {
    doFocus(
      "class →A← { def m = 1 }",
      ("class A { →def m = 1← }", down)
    )
  }

  test("Shorcut: object name to stat") {
    doFocus(
      "object →A← { def m1 = 1 }",
      ("object A { →def m1 = 1← }", down)
    )
  }

  test("focus from offset") {
    doFocus(
      "→class A←; class B",
      ("class A; →class B←", right)
    )
  }

  test("focus from offset") {
    doFocus(
      """|object A {
         |  def m = {
         |    foo→←bar
         |  }
         |}""".stripMargin,
      (
        """|object A {
           |  def m = {
           |    →foobar←
           |  }
           |}""".stripMargin,
        noop
      )
    )
  }

  test("focus from offset") {
    doFocus(
      "class A { private →←val a = 1; private val b = 2}",
      ("class A { private val a = 1; →private val b = 2←}", right)
    )
  }
}
