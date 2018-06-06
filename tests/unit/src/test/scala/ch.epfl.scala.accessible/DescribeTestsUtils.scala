package ch.epfl.scala.accessible

import scala.meta._
import scala.meta.testkit.DiffAssertions
import org.scalameta.logger
import scala.meta.parsers.Parse

trait DescribeTestsUtils extends FunSuite with DiffAssertions with MarkerTestUtils {

  trait ExtractFrom[T] { type From <: Tree }

  implicit val covariantTypeParam: ExtractFrom[Mod.Covariant] =
    new ExtractFrom[Mod.Covariant] { type From = Type.Param }

  implicit val contravariantTypeParam: ExtractFrom[Mod.Contravariant] =
    new ExtractFrom[Mod.Contravariant] { type From = Type.Param }

  implicit def extractFromItself[T <: Tree]: ExtractFrom[T] =
    new ExtractFrom[T] { type From = T }

  def check[T <: Tree](
      annotedSource: String,
      expected: String,
      parser: Parse[_ <: Tree] = Parse.parseStat,
      dialect: Dialect = dialects.Scala212)(implicit project: ExtractFrom[T]): Unit = {
    val source = removeSourceAnnotations(annotedSource)

    val testName = logger.revealWhitespace(source)
    test(testName) {
      val tree = parser(Input.String(source), dialect).get
      val subTree = findTree(tree, selection(annotedSource)).asInstanceOf[project.From]
      val obtained = Describe(subTree)
      assertNoDiff(obtained, expected)
    }
  }

  private def findTree(root: Tree, range: Range): Tree = {
    def isWithin(tree: Tree): Boolean =
      tree.pos.start <= range.start && range.end <= tree.pos.end

    def isFound(tree: Tree): Boolean =
      tree.pos.start == range.start && range.end == tree.pos.end

    var found: Option[Tree] = None
    object navigate extends Traverser {
      override def apply(tree: Tree): Unit = {
        if (isWithin(tree)) {
          if (isFound(tree)) {
            found = Some(tree)
          }

          super.apply(tree)
        }
      }
    }

    navigate(root)

    found.getOrElse(throw new Exception(s"cannot find tree within: [${range.start}, ${range.end}]"))
  }
}
