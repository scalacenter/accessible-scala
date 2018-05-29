package ch.epfl.scala.accessible

import scala.scalajs.js
import scala.scalajs.js.{UndefOr, |}
import scala.scalajs.js.annotation._

@js.native
@JSGlobalScope
object Globals extends js.Object {
  val meSpeak: Mespeak = js.native
}

trait Mespeak extends js.Object {
  def canPlay(): Boolean
  def getDefaultVoice(): String
  def getVolume(): Double //[0, 1]
  def isConfigLoaded(): Boolean
  def isVoiceLoaded(): Boolean
  def play(): Long
  def resetQueue(): Unit
  def setDefaultVoice(): Unit
  def setVolume(volume: Double): Unit
  def speak(text: String, args: SpeakOptions): Long
  def speakMultipart(parts: js.Array[SpeakPart], args: SpeakOptions): Long
  def stop(): Unit
}

trait SpeakPart extends SpeakOptions {
  val text: String
}

trait SpeakOptions extends js.Object {
  val amplitude: UndefOr[Int] = js.undefined
  val pitch: UndefOr[Int] = js.undefined
  val speed: UndefOr[Int] = js.undefined
  val wordgap: UndefOr[Int] = js.undefined
  val variant: UndefOr[String] = js.undefined
  val linebreak: UndefOr[Int] = js.undefined
  val capitals: UndefOr[Int] = js.undefined
  val punct: UndefOr[Boolean | String] = js.undefined
  val nostop: UndefOr[Boolean] = js.undefined
  val utf16: UndefOr[Boolean] = js.undefined
  val ssml: UndefOr[Boolean] = js.undefined
  val volume: UndefOr[Double] = js.undefined
  val rawdata: UndefOr[String] = js.undefined
  val log: UndefOr[Boolean] = js.undefined
}
