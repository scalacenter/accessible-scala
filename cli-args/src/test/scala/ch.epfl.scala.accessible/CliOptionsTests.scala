package ch.epfl.scala.accessible

import utest._

import java.nio.file.Paths

object CliOptionsTests extends TestSuite {
  val tests = Tests {
    "parse" - {
      val args = Array(
        "navigate",
        "--start=1851",
        "--end=1851",
        "--direction=right",
        "--file=/dev/null",
        "--output=voice"
      )
      val obtained = CliOptions(args)
      val expected = Some(
        CliOptions(
          CliCommand.Navigate(
            Direction.Right,
            Range(1851, 1851)
          ),
          CodeInput.FromPath(Paths.get("/dev/null")),
          OutputType.Voice
        )
      )
      assert(obtained == expected)
    }

    "help" - {
      CliOptions(Array("--help"))
    }

    "version" - {
      CliOptions(Array("--version"))
    }
  }
}
