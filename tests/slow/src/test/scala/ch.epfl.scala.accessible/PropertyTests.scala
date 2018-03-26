package ch.epfl.scala.accessible

import scala.meta._
import scala.meta.testkit.Corpus
import java.nio.charset.StandardCharsets

import utest._

object PropertyTests extends TestSuite {
  val tests = Tests {
    'main - {
      val corpus = Corpus.files(Corpus.fastparse)
      val corpusByFile =
        corpus.toList
          .groupBy { input =>
            import input._
            s"$user/${repo}${filename}"
          }
          .mapValues(_.head)
      val subject =
        "akka/akka/akka-actor/src/main/scala/akka/actor/AbstractActor.scala"
      val file = corpusByFile(subject)
      val jFile = file.jFile
      val input = Input.File(jFile, StandardCharsets.UTF_8)
      val tree = input.parse[Source].get

      val obtained = Summary(tree)
      val expected =
        """|package akka actor.
           |object AbstractActor,
           |class AbstractActor,
           |class AbstractLoggingActor,
           |class AbstractActorWithStash,
           |class AbstractActorWithUnboundedStash,
           |class AbstractActorWithUnrestrictedStash.""".stripMargin

      assert(obtained == expected)
      // Espeak(obtained)
    }
  }
}
