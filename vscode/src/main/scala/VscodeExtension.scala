import scala.scalajs.js
import scala.scalajs.js.annotation._

@js.native
@JSImport("vscode", JSImport.Namespace)
object vscode extends js.Object {
  val window: Window = js.native
}

trait Window extends js.Object {
  def showInformationMessage(message: String): Unit
}

object VscodeExtension {
  @JSExportTopLevel("activate")
  def activate(): Unit = {
    Mespeak.loadConfig(MespeakConfig)
    Mespeak.loadVoice(`en/en-us`)
    val message = "Welcome to accessible-scaa-laa demo!"
    vscode.window.showInformationMessage(message)
    Mespeak.speak(message, new SpeakOptions {
      override val speed = 300
    })
  }
}