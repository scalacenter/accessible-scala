package ch.epfl.scala.accessible

import scala.meta._
import scala.collection.mutable.StringBuilder

object Summary {

  def apply(tree: Tree): String = visit(tree)
  def visit(tree: Tree): String = tree match {
    case Source(stats) =>
      stats.map(visit).mkString("," + nl)

    case Pkg(ref, stats) =>
      val name = ref.toString.replaceAllLiterally(".", " ")
      s"package $name." + nl +
        stats.map(visit).mkString("", "," + nl, ".")

    case obj: Defn.Object =>
      s"object ${obj.name}"

    case cls: Defn.Class =>
      s"class ${cls.name}"

    case e => sys.error(e.getClass.toString)
  }
  private val nl = "\n"
}
