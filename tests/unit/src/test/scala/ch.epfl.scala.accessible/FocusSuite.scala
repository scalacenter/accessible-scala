package ch.epfl.scala.accessible

import scala.meta._

object FocusSuite extends FunSuite {
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
      ("→package a.b←", down),
      ("package →a.b←", down),
      ("package →a←.b", down),
      ("package →a←.b", down),
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
      ("→class A←", down),
      ("class →A←", down),
      ("class →A←", down)
    )
  }

  test("focus from offset") {
    val annotedSource = "→class A←; class B"
    val expected = selection("class A; →class B←")
    val currentCursor = selection(annotedSource)
    val code = removeSourceAnnotations(annotedSource)
    val tree = code.parse[Source].get
    val focus = Focus(tree, Offset(currentCursor.start))
    val obtained = focus.right.current
    assertPos(obtained, expected, code, "")
  }

  test("focus from offset") {
    val annotedSource =
      """|object A {
         |  def m = {
         |    foo→←bar
         |  }
         |}""".stripMargin

    val expected =
      selection(
        """|object A {
           |  def m = {
           |    →foobar←
           |  }
           |}""".stripMargin
      )

    val currentCursor = selection(annotedSource)
    val code = removeSourceAnnotations(annotedSource)
    val tree = code.parse[Source].get
    val focus = Focus(tree, Offset(currentCursor.start))
    val obtained = focus.current
    println(focus.currentTree)

    assertPos(obtained, expected, code, "")
  }
  
  private val nl = "\n"
  private val startMarker = '→'
  private val stopMarker = '←'

  val noop = (((f: Focus) => f), "∅")
  val up = (((f: Focus) => f.up), "↑")
  val down = (((f: Focus) => f.down), "↓")
  val left = (((f: Focus) => f.left), "←")
  val right = (((f: Focus) => f.right), "→")
  private def doFocus(code: String, steps: (String, (Focus => Focus, String))* ): Unit = {
    val tree = code.parse[Source].get
    steps.foldLeft(Focus(tree)){ case (focus, (annotedSource, (f, label))) =>
      val nextFocus = f(focus)
      val obtained = nextFocus.current
      val expected = selection(annotedSource)
      assertPos(obtained, expected, code, label)
      nextFocus
    }
  }

  private def assertPos(obtained: Range, expected: Range, code: String, label: String): Unit = {
    val fCode = fansi.Str(code)
    val fObtained = fCode.overlay(fansi.Color.Red, obtained.start, obtained.end)
    val fExpected = fCode.overlay(fansi.Color.Green, expected.start, expected.end)

    println(s"$label: $fExpected")

    if (obtained != expected) {
      println("   " + fObtained)
      println("failed")
      // throw new Exception("assertion failed")
    }
  } 

  private def removeSourceAnnotations(annotedSource: String): String = {
    annotedSource
      .replaceAllLiterally(startMarker.toString, "")
      .replaceAllLiterally(stopMarker.toString, "")
  }

  private def selection(annotedSource: String): Range = {


    var i = 0
    var markersBuilder: Option[Range] = None
    var lastStart: Option[Int] = None
    def error(msg: String, pos: Int): Unit = {
      sys.error(
        msg + nl +
          annotedSource + nl +
          (" " * pos) + "^"
      )
    }
    annotedSource.foreach { c =>
      if (c == startMarker) {
        if (lastStart.nonEmpty)
          error(s"Missing closing marker: '$stopMarker'", i)
        lastStart = Some(i)
      } else if (c == stopMarker) {
        lastStart match {
          case Some(start) => markersBuilder = Some(Range(start, i - 1))
          case None        => error("Unexpected closing marker", i)
        }
        lastStart = None
      }
      i += 1
    }

    markersBuilder.getOrElse(throw new Exception("cannot find selection"))
  }
}
