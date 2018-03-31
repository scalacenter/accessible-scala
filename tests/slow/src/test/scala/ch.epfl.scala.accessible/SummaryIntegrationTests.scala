package ch.epfl.scala.accessible

import scala.util.Try
import scala.meta._
import scala.meta.testkit.{Corpus, CorpusFile}
import java.nio.file.Files
import java.nio.charset.StandardCharsets

import scala.meta.testkit.DiffAssertions

import utest._

object SummaryIntegrationTests extends TestSuite with DiffAssertions {
  val corpus = Corpus.files(Corpus.fastparse)
  val corpusByFile =
    corpus.toList
      .groupBy(input => input.repo + input.filename)
      .mapValues(_.head)

  def getSummary(path: String): String =
    getSummary(path, None)

  def getSummary(path: String, offset: Option[Offset]): String = {
    getSummary(corpusByFile(path), offset)
  }

  def getSummary(file: CorpusFile): String = {
    getSummary(file, None)
  }

  def getSummary(file: CorpusFile, offset: Option[Offset]): String = {
    getTree(file).map(f => Summary(f, offset)).getOrElse("--cannot parse--")
  }

  def getTree(file: CorpusFile): Option[Source] = {
    val text =
      new String(Files.readAllBytes(file.jFile.toPath), StandardCharsets.UTF_8)
    val input = Input.String(text)
    Try(input.parse[Source].toOption).toOption.flatten
  }

  val tests = Tests {
    "top level summary" - {
      val path = "akka/akka-actor/src/main/scala/akka/actor/AbstractActor.scala"
      val obtained = getSummary(path)
      val expected =
        """|package akka actor.
           |object AbstractActor,
           |class AbstractActor,
           |class AbstractLoggingActor,
           |class AbstractActorWithStash,
           |class AbstractActorWithUnboundedStash,
           |class AbstractActorWithUnrestrictedStash.""".stripMargin
      assertNoDiff(obtained, expected)
    }

    "class summary" - {
      val path = "spire/core/shared/src/main/scala/spire/math/Algebraic.scala"

      val tree = getTree(corpusByFile(path)).get
      val position =
        tree.stats match {
          case List(Pkg(_, List(Pkg(_, stats)))) => 
            stats.find{
              case Defn.Object(_, Term.Name("Algebraic"), _) => true
              case _ => false
            }.get.pos
          case _ => ???
        }

      val obtained = getSummary(path, Some(Offset(position.start)))
      val expected = 
        """|object Algebraic: 
           |val Zero,
           |val One,
           |def apply,
           |def apply,
           |def apply,
           |def apply,
           |def apply,
           |def apply,
           |def apply,
           |def root,
           |def roots,
           |def unsafeRoot,
           |def apply,
           |class Expr,
           |object Expr,
           |class BitBound,
           |object BitBound,
           |def nrootApprox,
           |def nroot,
           |val bits2dec,
           |def nroot,
           |def nroot,
           |val JBigDecimalOrder,
           |def roundExact,
           |def roundPositive,
           |val MaxIntValue,
           |val MinIntValue,
           |val MaxLongValue,
           |val MinLongValue,
           |class ZeroBoundFunction,
           |object LiYap,
           |object BFMSS.""".stripMargin

      assertNoDiff(obtained, expected)
    }
  }
}
