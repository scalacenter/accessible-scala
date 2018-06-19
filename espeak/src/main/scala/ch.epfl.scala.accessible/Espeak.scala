package ch.epfl.scala.accessible

import scalanative.native
import scalanative.native._

class Espeak() {

  import espeakH._
  import EspeakError._

  private val bufferSizeInMilliseconds = 1000
  espeakng.initialize(AudioOutput.Playback, bufferSizeInMilliseconds, null, 0)

  def apply(text: String, synchronize: Boolean): Unit = {
    if (espeakng.isPlaying()) espeakng.cancel()

    native.Zone { implicit z =>
      val textPtr = toCString(text)
      val uuid = stackalloc[CUnsignedInt]
      val result = espeakng.synth(
        text = textPtr,
        size = text.size + 1,
        position = 0.toUInt,
        positionType = PositionType.Character,
        endPosition = 0.toUInt,
        flags = SynthFlags.CharsAuto,
        uniqueIdentifier = uuid,
        userData = null
      )

      result match {
        case Ok            => ()
        case InternalError => println("Espeak: InternalError")
        case BufferFull    => println("Espeak: BufferFull")
        case NotFound      => println("Espeak: NotFound")
        case e             => println(s"Espeak: other error ${e.value}")
      }

      if (synchronize) espeakng.synchronize()
    }
  }
  def stop(): Unit = espeakng.cancel()
}
