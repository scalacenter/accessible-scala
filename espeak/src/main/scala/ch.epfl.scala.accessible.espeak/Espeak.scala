package ch.epfl.scala.accessible.espeak

class Espeak {
  def stop(): Unit = nativeStop()
  def synthesize(text: String): Unit = nativeSynthesize(text)
  def synchronize(): Unit = nativeSynchronize()

  @native private final def nativeSynthesize(text: String): Unit

  @native private final def nativeStop(): Unit

  @native private final def nativeSynchronize(): Unit
}