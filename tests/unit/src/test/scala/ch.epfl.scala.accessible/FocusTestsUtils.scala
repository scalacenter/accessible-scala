package ch.epfl.scala.accessible

import scala.meta._

trait FocusTestsUtils extends FunSuite {
  val nl = "\n"
  val startMarker = '→'
  val stopMarker = '←'

  val noop = (((f: Focus) => f), "∅")
  val up = (((f: Focus) => f.up), "↑")
  val down = (((f: Focus) => f.down), "↓")
  val left = (((f: Focus) => f.left), "←")
  val right = (((f: Focus) => f.right), "→")

  def doFocus(codeInput: String, steps: (String, (Focus => Focus, String))* ): Unit = {
    val hasStartOffset = codeInput.contains(stopMarker) || codeInput.contains(stopMarker)

    if(!hasStartOffset) {
      val code = codeInput
      val tree = code.parse[Source].get
      doFocus0(code, Focus(tree), steps: _*)
    } else {
      val code = removeSourceAnnotations(codeInput)
      val currentCursor = selection(codeInput)
      val tree = code.parse[Source].get
      val initialFocus = Focus(tree, Offset(currentCursor.start))
      doFocus0(code, initialFocus, steps: _*)  
    }
  }

  private def doFocus0(code: String, initialFocus: Focus, steps: (String, (Focus => Focus, String))* ): Unit = {
    steps.foldLeft(initialFocus){ case (focus, (annotedSource, (f, label))) =>
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
