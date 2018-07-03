package ch.epfl.scala.accessible

import scala.scalanative.native._
// import scala.scalanative.native.extern

@link("espeak-ng")
@extern
object espeakng {
  import espeakH._

  @name("espeak_Initialize")
  def initialize(
      output: AudioOutput,
      buflength: CInt,
      path: CString,
      options: CInt
  ): Initialize = extern

  @name("espeak_Synth")
  def synth(
      text: Ptr[CChar],
      size: CSize,
      position: CUnsignedInt,
      positionType: PositionType,
      endPosition: CUnsignedInt,
      flags: SynthFlags,
      uniqueIdentifier: Ptr[CUnsignedInt],
      userData: Ptr[Byte]
  ): EspeakError = extern

  @name("espeak_Cancel")
  def cancel(): EspeakError = extern

  @name("espeak_Synchronize")
  def synchronize(): EspeakError = extern

  @name("espeak_IsPlaying")
  def isPlaying(): Boolean = extern
}

object espeakH {
  class Initialize(val value: CInt) extends AnyVal
  object Initialize {
    final val Error = new Initialize(-1)
    def SampleRate(hz: CInt) = new Initialize(hz)
  }

  class AudioOutput(val value: CInt) extends AnyVal
  object AudioOutput {
    final val Playback = new AudioOutput(0)
    final val Retrieval = new AudioOutput(1)
    final val Synchronous = new AudioOutput(2)
    final val SynchPlayback = new AudioOutput(3)
  }

  class PositionType(val value: CInt) extends AnyVal
  object PositionType {
    final val Character = new PositionType(1)
    final val Word = new PositionType(2)
    final val Sentence = new PositionType(3)
  }

  class EspeakError(val value: CInt) extends AnyVal
  object EspeakError {
    final val Ok = new EspeakError(0)
    final val InternalError = new EspeakError(1)
    final val BufferFull = new EspeakError(2)
    final val NotFound = new EspeakError(3)
  }

  class SynthFlags(val value: CInt) extends AnyVal
  object SynthFlags {
    final val CharsAuto = new SynthFlags(0)
    final val CharsUtf8 = new SynthFlags(1)
    final val Chars8bit = new SynthFlags(2)
    final val CharsWchar = new SynthFlags(3)
    final val Chars16bit = new SynthFlags(4)
  }

}
