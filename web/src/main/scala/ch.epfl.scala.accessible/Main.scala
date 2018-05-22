package ch.epfl.scala.accessible

import org.scalajs.dom.{document, window, console}
import org.scalajs.dom.raw.HTMLTextAreaElement
import scala.scalajs.js
import codemirror._
import scala.meta._

object Main {
  def main(args: Array[String]): Unit = {
    import Mespeak._
    Mespeak.loadConfig(MespeakConfig)
    loadVoice(`en/en-us`)

    speak("Welcome to accessible-scaa-laa demo!")

    CLike
    Sublime

    val code = Example.code

    val isMac = window.navigator.userAgent.contains("Mac")
    val ctrl = if (isMac) "Cmd" else "Ctrl"

    CodeMirror.keyMap.sublime -= "Ctrl-L"

    val darkTheme = "solarized dark"
    val lightTheme = "solarized light"

    val options = js
      .Dictionary[Any](
        "autofocus" -> true,
        "mode" -> "text/x-scala",
        "theme" -> darkTheme,
        "keyMap" -> "sublime",
        "extraKeys" -> js.Dictionary(
          "scrollPastEnd" -> false,
          "F2" -> "toggleSolarized",
          s"$ctrl-B" -> "browse",
          "Tab" -> "defaultTab",
          "Alt-Right" -> "goRight",
          "Alt-Left" -> "goLeft",
          "Alt-Up" -> "goUp",
          "Alt-Down" -> "goDown"
        )
      )
      .asInstanceOf[codemirror.Options]

    CodeMirror.commands.toggleSolarized = (editor: Editor) => {
      val key = "theme"

      val currentTheme = editor.getOption(key).asInstanceOf[String]
      val nextTheme =
        if (currentTheme == darkTheme) lightTheme
        else darkTheme

      editor.setOption(key, nextTheme)
    }

    val textArea = document.createElement("textarea").asInstanceOf[HTMLTextAreaElement]
    document.body.appendChild(textArea)

    val editor =
      CodeMirror.fromTextArea(
        textArea,
        options
      )

    editor.getDoc().setValue(code)

    def setSel(editor: Editor, pos: Range): Unit = {
      val doc = editor.getDoc()
      val start = doc.posFromIndex(pos.start)
      val end = doc.posFromIndex(pos.end)
      doc.setSelection(start, end)
      editor.scrollIntoView(start, 10)
    }

    var lastUtterance: Option[Int] = None

    def runCursor(editor: Editor, action: Cursor => Cursor): Unit = {
      val doc = editor.getDoc()
      val code = doc.getValue()

      code.parse[Source] match {
        case Parsed.Success(tree) =>
          val selections = doc.listSelections()

          val range =
            if (selections.size >= 1) {
              val selection = selections.head
              val start = doc.indexFromPos(selection.anchor)
              val end = doc.indexFromPos(selection.head)
              Range(start, end)
            } else {
              val cursor = doc.getCursor()
              val offset = doc.indexFromPos(cursor)
              Range(offset, offset)
            }

          val cursor = Cursor(tree, range)
          val nextCursor = action(cursor)
          setSel(editor, nextCursor.current)

          lastUtterance.foreach(id => stop(id))
          lastUtterance = Some(speak(Summary(nextCursor.tree)))


        case _ => ()
      }
    }

    CodeMirror.commands.goRight = (editor: Editor) => runCursor(editor, _.right)
    CodeMirror.commands.goLeft = (editor: Editor) => runCursor(editor, _.left)
    CodeMirror.commands.goUp = (editor: Editor) => runCursor(editor, _.up)
    CodeMirror.commands.goDown = (editor: Editor) => runCursor(editor, _.down)
  }
}
