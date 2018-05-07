package codemirror

import org.scalajs.dom.raw.{Event, Element, HTMLElement, HTMLTextAreaElement, KeyboardEvent}

import scala.scalajs.js
import js.annotation._
import js.{Dictionary, RegExp, UndefOr, |}

@js.native
@JSImport("codemirror", JSImport.Namespace)
object CodeMirror extends js.Object {
  var keyMap: KeyMaps = js.native
  def fromTextArea(textarea: HTMLTextAreaElement, options: Options): TextAreaEditor = js.native
  var commands: js.Dynamic = js.native
}

@js.native
@JSGlobal("Doc")
class Document protected () extends js.Object {
  def setValue(content: String): Unit = js.native
  def setCursor(pos: Position): Unit = js.native
  def setSelection(start: Position, end: Position): Unit = js.native
  def posFromIndex(index: Int): Position = js.native
  def indexFromPosition(pos: Position): Int = js.native
}

@js.native
@JSGlobal("CodeMirror.Pos")
class Position extends js.Object {
  var line: Int = js.native
  var ch: Int = js.native
}

trait KeyMaps extends js.Object {
  val sublime: js.Dictionary[String]
}

trait TextAreaEditor extends Editor {
  def save(): Unit
  def toTextArea(): Unit
  def getTextArea: HTMLTextAreaElement
}

trait Options extends js.Object {
  val autofocus: UndefOr[Boolean]
  val extraKeys: UndefOr[Dictionary[String]]
  val indentWithTabs: UndefOr[Boolean]
  val keyMap: UndefOr[String]
  val lineNumbers: UndefOr[Boolean]
  val lineWrapping: UndefOr[Boolean]
  val mode: UndefOr[String | js.Object]
  val scrollbarStyle: UndefOr[String]
  val scrollPastEnd: UndefOr[String]
  val showCursorWhenSelecting: UndefOr[Boolean]
  val smartIndent: UndefOr[Boolean]
  val tabindex: UndefOr[Int]
  val tabSize: UndefOr[Int]
  val theme: UndefOr[String]
  val value: UndefOr[String]
}

trait Editor extends js.Object {
  def getDoc(): Document
  def setOption(option: String, value: js.Any): Unit
  def getOption(option: String): js.Any
  def scrollIntoView(what: Position, margin: UndefOr[Int]): Unit
  protected[codemirror] def on(t: String, f: js.Function): Unit

}

object EditorExtensions {
  implicit class EditorEventHandler(val editor: Editor) extends AnyVal {
    def onKeyDown(f: (Editor, KeyboardEvent) => Unit): Unit = editor.on("keydown", f)
  }
}

@JSImport("codemirror/mode/clike/clike", JSImport.Namespace)
@js.native
object CLike extends js.Object

@JSImport("codemirror/keymap/sublime", JSImport.Namespace)
@js.native
object Sublime extends js.Object
