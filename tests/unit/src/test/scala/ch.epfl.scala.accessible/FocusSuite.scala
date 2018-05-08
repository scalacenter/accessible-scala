package ch.epfl.scala.accessible

import scala.meta._

object FocusSuite extends FunSuite {
  private val noop = (((f: Focus) => f), "∅")
  private val up = (((f: Focus) => f.up), "↑")
  private val down = (((f: Focus) => f.down), "↓")
  private val left = (((f: Focus) => f.left), "←")
  private val right = (((f: Focus) => f.right), "→")

  test("current") {
    doFocus(
      "class A; class B { val c = 1 }",
      ("→class A; class B { val c = 1 }←", noop)
    )
  }
  test("down right") {
    doFocus(
      "class A; class B { val c = 1 }",
      ("→class A←; class B { val c = 1 }", down),
      ("class A; →class B { val c = 1 }←", right)
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
      "class A",
      ("→class A←", down),
      ("class →A←", down),
      ("class →A←", down)
    )
  }

  test("shortcut class name to stats, val name to rhs") {
    doFocus(
      "class B { val c = 1 }",
      ("→class B { val c = 1 }←", down),
      ("class →B← { val c = 1 }", down),
      ("class B { →val c = 1← }", down),
      ("class B { val →c← = 1 }", down),
      ("class B { val c = →1← }", down)
    )
  }
  
  private val nl = "\n"
  private def doFocus(code: String, steps: (String, (Focus => Focus, String))* ): Unit = {
    val tree = code.parse[Source].get
    val focus = Focus(tree)

    steps.foreach{ case (annotedSource, (f, label)) =>
      f(focus)
      val obtained = focus.current
      val expected = selection(annotedSource)

      val fCode = fansi.Str(code)
      val fObtained = fCode.overlay(fansi.Color.Red, obtained.start, obtained.end)
      val fExpected = fCode.overlay(fansi.Color.Green, expected.start, expected.end)

      println(s"$label: $fExpected")

      val diff = 
        s"""|
            |$fObtained (obtained)
            |$fExpected (expected)""".stripMargin


      if (obtained != expected) {
        println(diff)
        throw new Exception("assertion failed")
      }
    }

  }
  private def selection(annotedSource: String): Range = {

    val startMarker = '→'
    val stopMarker = '←'

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
