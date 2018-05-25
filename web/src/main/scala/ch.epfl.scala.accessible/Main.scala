package ch.epfl.scala.accessible

import org.scalajs.dom.{document, console, window}
import org.scalajs.dom.raw.HTMLTextAreaElement
import scala.scalajs.js
import codemirror._
import scala.meta._

object Main {
  def main(args: Array[String]): Unit = {
    import CodeMirrorExtensions._
    // import Mespeak._
    Mespeak.loadConfig(MespeakConfig)
    Mespeak.loadVoice(`en/en-us`)

    var speechOn = true
    def speak(utterance: String, force: Boolean = false, punctuation: Boolean = true): Unit = {
      if (speechOn || force) {
        Mespeak.stop()
        Mespeak.speak(utterance, new SpeakOptions {
          override val speed = 250
          override val punct = punctuation
        })
      }
    }

    Mespeak.speak("Welcome to accessible-scaa-laa demo!", new SpeakOptions {
      override val speed = 300
    })
    console.log("Welcome to accessible-scaa-laa demo!")

    CLike
    Sublime

    val isMac = window.navigator.userAgent.contains("Mac")
    val ctrl = if (isMac) "Cmd" else "Ctrl"

    CodeMirror.keyMap.sublime -= s"$ctrl-L" // go to address bar

    val darkTheme = "solarized dark"
    val lightTheme = "solarized light"

    def setSel(editor: Editor, pos: Range): Unit = {
      val doc = editor.getDoc()
      val start = doc.posFromIndex(pos.start)
      val end = doc.posFromIndex(pos.end)
      val options = new SelectionOptions {
        override val scroll = false
        override val origin = "meta"
      }
      doc.setSelection(start, end, options)
      editor.scrollIntoView(start, 10)
    }

    def withTree(editor: Editor)(f: (Tree, Range) => Unit): Unit = {
      val doc = editor.getDoc()
      val code = doc.getValue()

      code.parse[Source] match {
        case Parsed.Success(tree) => {
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

          f(tree, range)
        }
        case Parsed.Error(pos, message, _) => {
          val range = Range(pos.start, pos.end)
          speak(message)
          setSel(editor, range)
        }
      }
    }

    def runCursor(action: Cursor => Cursor): js.Function1[Editor, Unit] = editor => {
      val doc = editor.getDoc()
      withTree(editor)((tree, range) => {
        val cursor = Cursor(tree, range)
        val nextCursor = action(cursor)
        setSel(editor, nextCursor.current)
        val summary = Describe(nextCursor.tree)
        
        if (summary.nonEmpty) {
          println(summary)
          speak(summary, punctuation = false)
        } else {
          // fallback to selected text
          val range = nextCursor.current

          val start = doc.posFromIndex(range.start)
          val end = doc.posFromIndex(range.end)

          val output = doc.getRange(start, end)
          speak(output)
        }
      })
    }

    def breadcrum(editor: Editor): Unit = {
      withTree(editor)((tree, range) => {
        val output = Breadcrumbs(tree, Offset(range.start))
        speak(output)
      })
    }

    def keyFun(body: Editor => Unit): js.Function1[Editor, Unit] =
      editor => body(editor)

    def toggleSpeech(editor: Editor): Unit = {
      speechOn = !speechOn
      val state =
        if (speechOn) "on"
        else "off"

      speak("speech " + state, force = true)
    }

    def toggleSolarized(editor: Editor): Unit = {
      val key = "theme"

      val currentTheme = editor.getOption(key).asInstanceOf[String]
      val nextTheme =
        if (currentTheme == darkTheme) lightTheme
        else darkTheme

      editor.setOption(key, nextTheme)
    }

    val options = js
      .Dictionary[Any](
        "autofocus" -> true,
        "mode" -> "text/x-scala",
        "theme" -> lightTheme,
        "keyMap" -> "sublime",
        "extraKeys" -> js.Dictionary(
          "scrollPastEnd" -> false,
          "Tab" -> "defaultTab",
          "F2" -> keyFun(toggleSolarized),
          "F3" -> keyFun(toggleSpeech),
          "Alt-Right" -> runCursor(_.right),
          "Alt-Left" -> runCursor(_.left),
          "Alt-Up" -> runCursor(_.up),
          "Alt-Down" -> runCursor(_.down),
          s"$ctrl-B" -> keyFun(breadcrum)
        )
      )
      .asInstanceOf[codemirror.Options]

    val textArea = document.createElement("textarea").asInstanceOf[HTMLTextAreaElement]
    document.body.appendChild(textArea)

    val editor =
      CodeMirror.fromTextArea(
        textArea,
        options
      )

    editor.onBeforeSelectionChange((editor, changes) => {

      val isCursorMoved =
        changes.origin.toOption.map(_ == "+move").getOrElse(false)

      if (isCursorMoved) {
        val doc = editor.getDoc()
        val range = changes.ranges.head
        val to = range.head
        val from = doc.getCursor()

        if (from.line == to.line) {
          val (min, max) = (from, to).sorted
          val content = doc.getRange(min, max)
          if (content.nonEmpty) {
            speak(content.trim)
          }
        }
      }
    })

    def speakPreviousWord(editor: Editor): Unit = {
      val doc = editor.getDoc()
      val cursor = doc.getCursor()
      val wordPos = editor.findPosH(cursor, -1, "word")

      val (min, max) = (wordPos, cursor).sorted
      val content = doc.getRange(min, max)
      speak(content)
    }

    val handledChars = (
      ('a' to 'z').toSet ++
        ('A' to 'Z').toSet ++
        ('1' to '9').toSet ++
        "[]{}()+-*/_".toSet
    ).map(_.toString)

    editor.onKeyDown((editor, event) => {
      val key = event.key
      val hasModKey = event.altKey || event.ctrlKey || event.metaKey
      if (handledChars.contains(key) && !hasModKey) {
        speak(key)
      } else if (key == " ") {
        speakPreviousWord(editor)
      } else if (key == "Up" || key == "Down") {
        val doc = editor.getDoc()
        speak(doc.getLine(doc.getCursor().line))
      }
    })

    val defaultCode = 
      """|class A {
         |  val a = 1
         |  val b = 2
         |}
         |
         |class B
         |
         |class D[T1, T2] extends B with C {
         |
         |}""".stripMargin

    val localStorageKey = "code"
    val initialCode = Option(window.localStorage.getItem(localStorageKey)).getOrElse(defaultCode)

    editor.getDoc().setValue(initialCode)
    editor.onChange((_, _) => {
      window.localStorage.setItem(localStorageKey, editor.getDoc().getValue())
    })
  }
}
