import ch.epfl.scala.accessible.{Cursor, Range, Describe}

import vscode.{Selection, ExtensionContext}

import scala.meta._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

object extension {
  @JSExportTopLevel("activate")
  def activate(context: ExtensionContext): Unit = {
    val aScala = new AccessibleScala()

    val commands = List(
      ("goLeft", aScala.runCursor(_.left)),
      ("goRight", aScala.runCursor(_.right)),
      ("goUp", aScala.runCursor(_.up)),
      ("goDown", aScala.runCursor(_.down))
    )

    commands.foreach {
      case (name, fun) =>
        context.subscriptions.push(
          vscode.commands.registerCommand(s"accessibleScala.$name", fun)
        )
    }
  }
}

class AccessibleScala() {
  val tts = TextToSpeech()

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
        val range = nextCursor.current

        val editor = vscode.window.activeTextEditor
        val document = editor.document

        val start = document.positionAt(range.start)
        val end = document.positionAt(range.end)

        val output = document.getText(new _root_.vscode.Range(start, end))
        tts.speak(output)
      }
    })
  }

  private def withTree(f: (Tree, Range) => Unit): Unit = {
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

  private def setSel(pos: Range): Unit = {
    val editor = vscode.window.activeTextEditor
    val document = editor.document
    val start = document.positionAt(pos.start)
    val end = document.positionAt(pos.end)
    editor.selection = new Selection(start, end)
  }
}
