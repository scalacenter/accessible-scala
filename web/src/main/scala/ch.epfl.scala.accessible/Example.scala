package ch.epfl.scala.accessible

object Example {
  val code =
    """
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package foo.bar

import scala.collection.Seq

sealed abstract class CS {

  def value: Byte

  def foo: Option[String] =
    if(true) Some("foo") else None

  protected def myParam: Boolean = true
}

object CS {

  // Leading Comment
  case object CS0 extends CS {
    def value = 0
    override protected def myParam: Boolean = false
  }

  case object CS1 extends CS {
    def value = 1
  }

  case object CS2 extends CS {
    def value = 2
    // Leading Comment 1
    // Leading Comment 2
    override def foo = Some("MS")
  }

  case object CS3 extends CS {
    def value = 3
  }

  case object CS4 extends CS {
    def value = 4
  }

  case object CS5 extends CS {
    def value = 5
  }

  case object CS6 extends CS {
    def value = 6
  }

  case object CS7 extends CS {
    def value = 7
  }

  case object CS8 extends CS {
    def value = 8
  }

  case object CS9 extends CS {
    def value = 9
  }

  case object CS10 extends CS {
    def value = 10
  }

  case object CS11 extends CS {
    def value = 11
  }

  case object CS12 extends CS {
    def value = 12
  }

  case object CS13 extends CS {
    def value = 13
  }

  val values: Seq[CS] = Seq(CS0, CS1, CS2, CS3, CS4, CS5, CS6, CS7, 
    CS8, CS9, CS10, CS11, CS12, CS13)
}
""".trim

  val code2 =
    """
import org.scalajs.dom.{document, console, window}
import org.scalajs.dom.raw.HTMLTextAreaElement
import org.scalajs.dom.ext.KeyCode

import scala.scalajs.js

import codemirror.{Position => CMPos, _}

import scala.meta._

object Main {
  def main(args: Array[String]): Unit = {
    // import Mespeak._
    // loadConfig(MespeakConfig)
    // loadVoice(`en/en-us`)
    // speak("Hello, World")

    CLike
    Sublime

    import EditorExtensions._

    val code = Example.code

    val isMac = window.navigator.userAgent.contains("Mac")
    val ctrl = if (isMac) "Cmd" else "Ctrl"

    CodeMirror.keyMap.sublime.delete("Ctrl-L")

    val darkTheme = "solarized dark"
    val lightTheme = "solarized light"

    val options = js.Dictionary[Any](
      "autofocus" -> true,
      "mode" -> "text/x-scala",
      "theme" -> lightTheme,
      "keyMap" -> "sublime",
      "extraKeys" -> js.Dictionary(
        "scrollPastEnd" -> false,
        "F2" -> "toggleSolarized",
        s"$ctrl-B" -> "browse",
        "Tab" -> "defaultTab"
      )
    ).asInstanceOf[codemirror.Options]

    CodeMirror.commands.toggleSolarized = (editor: Editor) => {
      val key = "theme"

      val currentTheme = editor.getOption(key).asInstanceOf[String]
      val nextTheme = 
        if(currentTheme == darkTheme) lightTheme
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

    val tree = code.parse[Source].get
    val focus = new Focus(tree)

    editor.onKeyDown((editor, keyEvent) => {
      val keyCode = keyEvent.keyCode
      val pos = 
        keyCode match {
          case KeyCode.Down => 
            keyEvent.preventDefault()
            focus.down
          case KeyCode.Up =>
            keyEvent.preventDefault()
            focus.up
          case KeyCode.Left => 
            keyEvent.preventDefault()
            focus.left
          case KeyCode.Right =>
            keyEvent.preventDefault()
            focus.right
          case _ =>
            focus.current
        }
      val doc = editor.getDoc()

      val start = doc.posFromIndex(pos.start)
      val end = doc.posFromIndex(pos.end)

      doc.setSelection(start, end)
    })

    editor.getDoc().setValue(code)
  }
}

case class Pos(start: Int, end: Int)

class Focus(tree: Tree) {
  private var parent = List(tree)
  private var children = Vector(tree)
  private var child = 0
    
  private def toPos(pos: Position): Pos = Pos(pos.start, pos.end)

  def current: Pos = toPos(children(child).pos)

  def down: Pos = {
    parent = children(child) :: parent
    if (children.nonEmpty) {
      children = children(child).children.toVector
      child = 0
    }
    current
  }
  def up: Pos = {
    if (parent.size > 1) {
      children = Vector(parent.head)
      child = 0
      parent = parent.tail
    }
    current
  }
  def left: Pos = {
    if(child > 0) {
      child -= 1
    }
    current
  }
  def right: Pos = {
    if (child < children.size) {
      child += 1
    }
    current
  }
}
""".trim

  val code3 =
    """
children(child).children.toVector
""".trim
}
