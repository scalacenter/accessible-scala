import scala.scalajs.js
import scala.scalajs.js.annotation._

@js.native
@JSImport("process", JSImport.Namespace)
object process extends js.Object {
  val platform: String = js.native
}

object TextToSpeech {
  def apply(): TextToSpeech = {
    process.platform match {
      case "win32"  => new SayJs()
      case "darwin" => new SayJs()
      case "linux"  => new Espeak()
      case e        => throw new Exception(s"unsupported platform: $e")
    }
  }
}

trait TextToSpeech {
  def stop(): Unit
  def speak(utterance: String)
}

@js.native
@JSImport("say", JSImport.Namespace)
object say extends js.Object {
  def speak(utterance: String): Unit = js.native
  def stop(): Unit = js.native
}

class SayJs() extends TextToSpeech {
  def stop(): Unit = say.stop()
  def speak(utterance: String) = say.speak(utterance)
}

// say does not support espeak: https://github.com/Marak/say.js/issues/14
class Espeak() extends TextToSpeech {
  import scala.collection.mutable.{Map => MMap}

  private val processes = MMap.empty[Int, Proccess]

  def stop(): Unit = processes.values.foreach(_.kill)

  def speak(utterance: String): Unit = {
    stop()
    val child = spawn("espeak", js.Array(utterance))
    child.on("exit", (_, _) => processes -= child.pid)
    processes += child.pid -> child
  }
}

@js.native
@JSImport("child_process", "spawn")
object spawn extends js.Object {
  def apply(command: String, args: js.Array[String]): Proccess = js.native
}

trait Proccess extends js.Object {
  def kill(): Unit
  def on(event: String, callback: js.Function2[Int, String, Unit]): Unit
  def pid: Int
}
