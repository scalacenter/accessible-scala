package ch.epfl.scala.accessible

import scala.scalajs.js
import scala.scalajs.js.{UndefOr, |}
import scala.scalajs.js.annotation._

trait Voice extends js.Object

@js.native
@JSImport("mespeak/voices/en/en-us.json", JSImport.Namespace)
object `en/en-us` extends Voice

trait Config extends js.Object

@js.native
@JSImport("mespeak/src/mespeak_config.json", JSImport.Namespace)
object MespeakConfig extends Config

@js.native
@JSImport("mespeak", JSImport.Namespace)
object Mespeak extends js.Object {
  def canPlay(): Boolean = js.native
  def getDefaultVoice(): String = js.native
  def getVolume(): Double = js.native // [0, 1]
  def isConfigLoaded(): Boolean = js.native
  def isVoiceLoaded(): Boolean = js.native
  def loadConfig(config: Config): Unit = js.native
  def loadVoice(voice: Voice): Unit = js.native
  def play(): Long = js.native
  def resetQueue(): Unit = js.native
  def setDefaultVoice(): Unit = js.native
  def setVolume(volume: Double): Unit = js.native
  def speak(text: String,
            args: SpeakOptions = js.native,
            callback: Boolean => Unit = js.native,
            _id: Long = js.native): Long = js.native
  def speakMultipart(parts: Array[SpeakPart],
                     args: SpeakOptions = js.native,
                     callback: Boolean => Unit = js.native,
                     _id: Long = js.native): Long = js.native
  def stop(id: UndefOr[Long] = js.native): Unit = js.native
}

trait SpeakPart extends SpeakOptions {
  val text: String
}

trait SpeakOptions extends js.Object {
  val amplitude: UndefOr[Int]
  val pitch: UndefOr[Int]
  val speed: UndefOr[Int]
  val voice: UndefOr[Voice]
  val wordgap: UndefOr[Int]
  val variant: UndefOr[String]
  val linebreak: UndefOr[Int]
  val capitals: UndefOr[Int]
  val punct: UndefOr[Boolean | String]
  val nostop: UndefOr[Boolean]
  val utf16: UndefOr[Boolean]
  val ssml: UndefOr[Boolean]
  val volume: UndefOr[Double]
  val rawdata: UndefOr[String]
  val log: UndefOr[Boolean]
}
