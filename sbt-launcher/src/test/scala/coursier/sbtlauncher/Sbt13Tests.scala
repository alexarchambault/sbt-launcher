package coursier.sbtlauncher

import java.nio.file.{Files, Paths}

import utest._

object Sbt13Tests extends TestSuite {

  import TestHelpers._

  val tests = Tests {

    "sbt 1.3" - {
      "0-RC2" - {
        "test" - {
          run(
            Paths.get(s"tests/test-sbt-coursier-sbt-1.3.0-RC2"),
            "1.3.0-RC2",
            sbtCommands = Seq("update", "updateClassifiers", "test:compile", "coursierDependencyTree"),
            forceSbtVersion = true
          )
        }
      }
    }

  }

}
