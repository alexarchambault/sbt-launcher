package coursier.sbtlauncher

import java.lang.ProcessBuilder.Redirect
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}

import utest._

object ProjectTests extends TestSuite {

  val launcher = {
    val p = Paths.get("target/test-csbt")
    val b = new ProcessBuilder("/bin/bash", "-c", "./generate-csbt.sh -r ivy2Local -f")
      .redirectOutput(Redirect.INHERIT)
      .redirectError(Redirect.INHERIT)
      .redirectInput(Redirect.PIPE)
    val env = b.environment()
    env.put("OUTPUT", p.toAbsolutePath.toString)
    env.put("VERSION", Properties.version)
    Console.err.println(s"Generating launcher $p")
    val proc = b.start()
    proc.getOutputStream.close()
    val retCode = proc.waitFor()
    assert(retCode == 0)
    Console.err.println(s"Generated launcher $p")
    p
  }

  def run(
    dir: Path,
    sbtVersion: String,
    sbtCommands: Seq[String] = Seq("update", "updateClassifiers", "test:compile", "test"),
    forceSbtVersion: Boolean = false
  ): Unit = {

    val propFile = dir.resolve("project/build.properties")

    val extraArgs =
      if (forceSbtVersion)
        Seq("--sbt-version", sbtVersion)
      else
        Nil

    if (!forceSbtVersion) {

      val actualSbtVersion = {
        val p = new java.util.Properties
        p.load(Files.newInputStream(propFile))
        Option(p.getProperty("sbt.version")).getOrElse {
          sys.error(s"No sbt version found in ${propFile.toAbsolutePath.normalize()}")
        }
      }

      assert(actualSbtVersion == sbtVersion)
    }

    val cmd = Seq(launcher.toAbsolutePath.toString) ++ extraArgs ++ sbtCommands
    Console.err.println("Running")
    Console.err.println(s"  ${cmd.mkString(" ")}")
    Console.err.println(s"in directory $dir")
    val p = new ProcessBuilder(cmd: _*)
      .directory(dir.toFile)
      .redirectOutput(Redirect.INHERIT)
      .redirectError(Redirect.INHERIT)
      .redirectInput(Redirect.PIPE)
      .start()
    p.getOutputStream.close()
    val retCode = p.waitFor()
    assert(retCode == 0)
  }

  def runCaseAppTest(sbtVersion: String): Unit =
    run(
      Paths.get(s"tests/case-app-sbt-$sbtVersion"),
      sbtVersion,
      sbtCommands = Seq("update", "updateClassifiers", "test:compile", "testsJVM/test"),
      forceSbtVersion = true
    )

  val tests = Tests {

    "sbt 0.13.17" - {
      "sourcecode" - {
        val dir = Paths.get("tests/sourcecode-sbt-0.13.17")
        run(dir, "0.13.17")
      }
    }

    "sbt 1" - {
      "0.0" - {
        "case-app" - {
          runCaseAppTest("1.0.0")
        }
      }
      "0.1" - {
        "case-app" - {
          runCaseAppTest("1.0.1")
        }
      }
      "0.2" - {
        "case-app" - {
          runCaseAppTest("1.0.2")
        }
      }
      "0.3" - {
        "case-app" - {
          runCaseAppTest("1.0.3")
        }
      }
      "0.4" - {
        "case-app" - {
          runCaseAppTest("1.0.4")
        }
      }
      "2.0" - {
        "case-app" - {
          runCaseAppTest("1.2.0")
        }
      }
      "2.1" - {
        "case-app" - {
          runCaseAppTest("1.2.1")
        }
      }
      "2.2" - {
        "case-app" - {
          runCaseAppTest("1.2.2")
        }
      }
      "2.3" - {
        "case-app" - {
          runCaseAppTest("1.2.3")
        }
        "sourcecode" - {
          val dir = Paths.get("tests/sourcecode-sbt-1.2.3")
          run(
            dir,
            "1.2.3",
            sbtCommands = Seq("+update", "+updateClassifiers", "+test:compile", "test")
          )
        }
      }
      "2.4" - {
        "case-app" - {
          runCaseAppTest("1.2.4")
        }
      }
      "2.5" - {
        "case-app" - {
          runCaseAppTest("1.2.5")
        }
      }
      "2.6" - {
        "case-app" - {
          runCaseAppTest("1.2.6")
        }
      }
      "2.7" - {
        "case-app" - {
          runCaseAppTest("1.2.7")
        }
      }
    }

    "reload" - {
      * - {
        val dir = Paths.get("tests/sourcecode-sbt-1.2.3")
        run(
          dir,
          "1.2.3",
          sbtCommands = Seq("test:compile", "reload", "test")
        )
      }
    }

  }

}
