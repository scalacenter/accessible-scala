package ch.epfl.scala.accessible

import java.nio.file.{Paths, Files}
import scala.meta._

object CursorPlayground extends CursorTestsUtils {
  test("playground") {
    val examplePath = Paths.get(this.getClass.getResource("/example.scala").toURI)
    val code = new String(Files.readAllBytes(examplePath))
    val tree = code.parse[Source].get
    val range = tree.collect { case t @ q"CS0" => Range(t.pos.start, t.pos.end) }.head
    val cursor = Cursor(tree, range)

    val obtained = cursor.up.right.right.right
    println(code.substring(obtained.current.start, obtained.current.end))



  }
}