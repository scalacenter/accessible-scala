package ch.epfl.scala.accessible

import scala.meta._

trait CursorTestsUtils extends FunSuite {
  val nl = "\n"
  val startMarker = '→'
  val stopMarker = '←'

  val noop = (((f: Cursor) => f), "∅")
  val up = (((f: Cursor) => f.up), "↑")
  val down = (((f: Cursor) => f.down), "↓")
  val left = (((f: Cursor) => f.left), "←")
  val right = (((f: Cursor) => f.right), "→")

  def doFocus(codeInput: String, steps: (String, (Cursor => Cursor, String))*): Unit = {
    val hasRange = codeInput.contains(stopMarker) || codeInput.contains(stopMarker)

    if (!hasRange) {
      val code = codeInput
      val tree = code.parse[Source].get
      doCursor0(code, Cursor(tree).current, steps: _*)
    } else {
      val code = removeSourceAnnotations(codeInput)
      val range = selection(codeInput)
      val tree = code.parse[Source].get
      val initialCursor = Cursor(tree, range)
      doCursor0(code, initialCursor.current, steps: _*)
    }
  }

  private def doCursor0(code: String,
                        initialSel: Range,
                        steps: (String, (Cursor => Cursor, String))*): Unit = {
    val tree = code.parse[Source].get
    steps.foldLeft(initialSel) {
      case (sel, (annotedSource, (f, label))) =>
        val currentCursor = Cursor(tree, sel)
        val nextCursor = f(currentCursor)
        val obtained = nextCursor.current
        val expected = selection(annotedSource)
        assertPos(obtained, expected, code, label)
        obtained
    }
  }

  private def assertPos(obtained: Range, expected: Range, code: String, label: String): Unit = {
    val fCode = fansi.Str(code)
    val fObtained = fCode.overlay(fansi.Color.Red, obtained.start, obtained.end)
    val fExpected = fCode.overlay(fansi.Color.Green, expected.start, expected.end)

    println(s"$label: $fExpected")

    if (obtained != expected) {
      println("   " + fObtained)
      throw new Exception("assertion failed")
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
