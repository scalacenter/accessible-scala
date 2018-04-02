package ch.epfl.scala.accessible.espeak

class Espeak {
  def stop(): Unit = nativeStop()
  def synthesize(text: String): Unit = nativeSynthesize(text)

  @native private final def nativeSynthesize(text: String): Unit

  @native private final def nativeStop(): Unit
}