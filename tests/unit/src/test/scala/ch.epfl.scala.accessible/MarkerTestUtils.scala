package ch.epfl.scala.accessible

trait MarkerTestUtils {
  protected val nl = "\n"
  protected val startMarker = '→'
  protected val stopMarker = '←'

  protected def removeSourceAnnotations(annotedSource: String): String = {
    annotedSource
      .replaceAllLiterally(startMarker.toString, "")
      .replaceAllLiterally(stopMarker.toString, "")
  }

  protected def selection(annotedSource: String): Range = {
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

    markersBuilder.getOrElse(Range(0, annotedSource.size))
  }
}