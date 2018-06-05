package ch.epfl.scala.accessible

object CursorPlayground extends CursorTestsUtils {
  test("navigate apply/select chains left to right") {
    // doFocus(
    //   "class A { foo.bar(arg).→buzz← }",
    //   ("class A { foo.bar(→arg←).buzz }", left)
    //   // ("class A { foo.→bar←(arg).buzz }", left),
    //   // ("class A { →foo←.bar(arg).buzz }", left),
    // )
  }
}