package ch.epfl.scala.accessible

import scala.util.Try
import scala.meta._
import scala.meta.testkit.{Corpus, CorpusFile}
import java.nio.file.Files
import java.nio.charset.StandardCharsets

import utest._

object SummaryIntegrationTests extends TestSuite {
  val corpus = Corpus.files(Corpus.fastparse)
  val corpusByFile =
    corpus.toList
      .groupBy(input => input.repo + input.filename)
      .mapValues(_.head)

  def getSummary(path: String): String = {
    getSummary(corpusByFile(path))
  }

  def getSummary(file: CorpusFile): String = {
    getTree(file).map(Summary(_)).getOrElse("--cannot parse--")
  }

  def getTree(file: CorpusFile): Option[Tree] = {
    val text =
      new String(Files.readAllBytes(file.jFile.toPath), StandardCharsets.UTF_8)
    val input = Input.String(text)
    Try(input.parse[Source].toOption).toOption.flatten
  }

  val tests = Tests {
    'summary - {
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
      assert(obtained == expected)
    }
  }
}
