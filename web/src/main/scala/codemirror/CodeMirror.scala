package codemirror

import org.scalajs.dom.raw.{HTMLTextAreaElement, KeyboardEvent}

import scala.scalajs.js
import js.annotation._
import js.{Dictionary, UndefOr, |, undefined}

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
  def getValue(separator: UndefOr[String] = undefined): String = js.native
  def getLine(n: Int): String = js.native
  def getRange(from: Position, to: Position): String = js.native
  def setValue(content: String): Unit = js.native
  def setCursor(pos: Position): Unit = js.native
  def getCursor(): Position = js.native
  def setSelection(anchor: Position,
                   head: UndefOr[Position] = undefined,
                   options: UndefOr[SelectionOptions] = undefined): Unit = js.native
  def posFromIndex(index: Int): Position = js.native
  def indexFromPos(pos: Position): Int = js.native
  def listSelections(): js.Array[Selection] = js.native
}

trait SelectionOptions extends js.Object {
  val scroll: UndefOr[Boolean] = js.undefined
  val origin: UndefOr[String] = js.undefined
  val bias: UndefOr[Int] = js.undefined
}

trait Selection extends js.Object {
  def anchor: Position
  def head: Position
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
  def findPosH(cursor: Position, amount: Int, unit: String): Position
  def getDoc(): Document
  def setOption(option: String, value: js.Any): Unit
  def getOption(option: String): js.Any
  def scrollIntoView(what: Position, margin: UndefOr[Int]): Unit
  protected[codemirror] def on(t: String, f: js.Function): Unit

}

trait SelectionChange extends js.Object {
  val origin: UndefOr[String]
  val ranges: js.Array[Range]
}

trait Range extends js.Object {
  val anchor: Position
  val head: Position
}

object CodeMirrorExtensions {
  implicit class EditorEventHandler(val editor: Editor) extends AnyVal {
    def onKeyDown(f: (Editor, KeyboardEvent) => Unit): Unit = editor.on("keydown", f)
    def onChange(f: (Editor, js.Object) => Unit): Unit =
      editor.on("change", f)
    def onBeforeSelectionChange(f: (Editor, SelectionChange) => Unit): Unit =
      editor.on("beforeSelectionChange", f)
    def onKeyHandled(f: (Editor, String, KeyboardEvent) => Unit): Unit = editor.on("keyHandled", f)
  }

  implicit class PositionExtensions(val pos: Position) extends AnyVal {
    def <(other: Position): Boolean = pos.line <= other.line && pos.ch < other.ch
  }

  implicit class PositionTupleExtensions(val ps: (Position, Position)) extends AnyVal {
    def sorted: (Position, Position) = {
      val (a, b) = ps

      if (a < b) (a, b)
      else (b, a)
    }
  }
}

@JSImport("codemirror/mode/clike/clike", JSImport.Namespace)
@js.native
object CLike extends js.Object

@JSImport("codemirror/keymap/sublime", JSImport.Namespace)
@js.native
object Sublime extends js.Object
