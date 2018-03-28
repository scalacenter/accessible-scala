package ch.epfl.scala.accessible

import java.lang.ProcessBuilder
import java.lang.ProcessBuilder.Redirect
import java.io.File

object Espeak {
  def apply(in: String): Unit = {
    val config = EspeakConfig()
    val args = config.toList
    val all = "espeak" :: args ::: List(in)

    val pb = new ProcessBuilder(all: _*)
    pb.redirectErrorStream(true)
    pb.redirectOutput(ProcessBuilder.Redirect.INHERIT)

    val process = pb.start()
    process.waitFor()
  }
}

case class EspeakConfig(
    // -a
    amplitude: Int = 100, // 0 to 200
    // -g
    wordGaps: Int = 10, // ms
    // -s
    wordsPerMinute: Int = 250
) {
  def toList: List[String] = {
    List(
      "-a",
      amplitude.toString,
      "-g",
      wordGaps.toString,
      "-s",
      wordsPerMinute.toString
    )
  }
}
