package ch.epfl.scala.accessible

import scala.meta._

object FocusSuite extends FunSuite {
  test("current") {
    doFocus(
      noop,
      """|class A
         |class B {
         |  val c = 1
         |}""".stripMargin,
      """|→class A
         |class B {
         |  val c = 1
         |}←""".stripMargin,
    )
  }
  test("down") {
    doFocus(
      _.down,
      """|class A
         |class B {
         |  val c = 1
         |}""".stripMargin,
      """|→class A←
         |class B {
         |  val c = 1
         |}""".stripMargin,
    )
  }
  test("down right") {
    doFocus(
      _.down.right,
      """|class A
         |class B {
         |  val c = 1
         |}""".stripMargin,
      """|class A
         |→class B {
         |  val c = 1
         |}←""".stripMargin
    )
  }
  test("preserve children position") {
    doFocus(
      _.down.right.down.up,
      """|class A
         |class B {
         |  val c = 1
         |}""".stripMargin,
      """|class A
         |→class B {
         |  val c = 1
         |}←""".stripMargin
    )
  }
  test("no children") {
    doFocus(_.down.down.down, "class A", "class →A←")
  }

  test("shortcut class name to template") {
    doFocus(
      _.down.down.down,
      """|class B {
         |  val c = 1
         |}""".stripMargin,
      """|class B {
         |  →val c = 1←
         |}""".stripMargin,
    )
  }

  test("shortcut val name to rhs") {
    doFocus(
      _.down.down.down.down.down,
      """|class B {
         |  var c = 1
         |}""".stripMargin,
      """|class B {
         |  var c = →1←
         |}""".stripMargin,
    )
  }

  // test("selects chains in-order") {
  //   doFocus(
  //     _.
  //     "a.b.c",
  //     "a.b.c"
  //   )
  // }

  private val nl = "\n"
  private def noop: Focus => Unit = _ => ()
  private def doFocus(f: Focus => Unit, code: String, annotedSource: String): Unit = {
    val tree = code.parse[Source].get
    val focus = Focus(tree)
    f(focus)
    val obtained = focus.current
    val expected = selection(annotedSource)

    val fCode = fansi.Str(code)
    val fObtained = fCode.overlay(fansi.Color.Red, obtained.start, obtained.end)
    val fExpected = fCode.overlay(fansi.Color.Green, expected.start, expected.end)

    val diff = nl + fObtained + nl + fExpected

    if (obtained != expected) {
      println(diff)
      throw new Exception("assertion failed")
    }
  }
  private def selection(annotedSource: String): Pos = {

    val startMarker = '→'
    val stopMarker = '←'

    var i = 0
    var markersBuilder: Option[Pos] = None
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
          case Some(start) => markersBuilder = Some(Pos(start, i - 1))
          case None        => error("Unexpected closing marker", i)
        }
        lastStart = None
      }
      i += 1
    }

    markersBuilder.getOrElse(throw new Exception("cannot find selection"))
  }
}
