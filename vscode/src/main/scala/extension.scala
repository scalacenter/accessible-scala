import scala.scalajs.js.annotation.JSExportTopLevel

import ch.epfl.scala.accessible.{Cursor, Range, Describe}
import _root_.vscode.{vscode, Selection, ExtensionContext}
import scala.scalajs.js
import scala.scalajs.js.annotation._

import scala.meta._

@js.native
@JSImport("./tts.js", JSImport.Namespace)
object tts extends js.Object {
  def speak(utterance: String): Unit = js.native
  def cancel(): Unit = js.native
}

object extension {

  def runCursor(action: Cursor => Cursor): js.Function = () => {
    withTree((tree, range) => {
      val cursor = Cursor(tree, range)
      val nextCursor = action(cursor)

      setSel(nextCursor.current)
      val summary = Describe(nextCursor.tree)

      if (summary.nonEmpty) {
        tts.speak(summary)
      } else {
        // fallback to selected text
        // val range = nextCursor.current

        // val editor = vscode.window.activeTextEditor
        // val document = editor.document

        // val start = document.positionAt(range.start)
        // val end = document.positionAt(range.end)

        // val output = document.getText(new _root_.vscode.Range(start, end))
        // tts.speak(output)

        
      }
    })
  }

  def withTree(f: (Tree, Range) => Unit): Unit = {
    val editor = vscode.window.activeTextEditor
    val document = editor.document
    val code = document.getText()

    code.parse[Source] match {
      case Parsed.Success(tree) => {
        val selection = editor.selection
        val start = document.offsetAt(selection.anchor)
        val end = document.offsetAt(selection.active)
        val range = Range(start, end)
        f(tree, range)
      }
      case Parsed.Error(pos, message, _) => {
        val range = Range(pos.start, pos.end)
        tts.speak(message)
        setSel(range)
      }
    }
  }

  def setSel(pos: Range): Unit = {
    val editor = vscode.window.activeTextEditor
    val document = editor.document
    val start = document.positionAt(pos.start)
    val end = document.positionAt(pos.end)
    editor.selection = new Selection(start, end)
  }

  @JSExportTopLevel("activate")
  def activate(context: ExtensionContext): Unit = {

    tts.speak("this is too long")
    tts.speak("yes it is")
    tts.speak("more text")

    val commands = List(
      ("goLeft",  runCursor(_.left)),
      ("goRight", runCursor(_.right)),
      ("goUp",    runCursor(_.up)),
      ("goDown",  runCursor(_.down))
    )

    commands.foreach{ case (name, fun) =>        
      context.subscriptions.push(
        vscode.commands.registerCommand(s"accessibleScala.$name", fun)
      )
    }
  }
}