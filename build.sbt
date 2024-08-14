import scala.language.postfixOps
import scala.scalanative.build.*
import scala.sys.process.*

scalaVersion := "3.4.1"
name := "scala-beast"
organization := "br.com.mobilemind.beast"

// set to Debug for compilation details (Info is default)
logLevel := Level.Info

lazy val appStop = inputKey[Unit]("stop app")
lazy val appRestart = inputKey[Unit]("run app")
lazy val showPid = inputKey[Unit]("show app PID")

lazy val routing = ProjectRef(file("../micro-routing"), "appNative")
lazy val jsonCodec = ProjectRef(file("../scala-json-codec"), "appNative")

scalacOptions ++= Seq(
  "-new-syntax",
  //"-no-indent",
  "-Wvalue-discard",
  "-Wunused:all",
  //"-Werror",
  "-deprecation",
  "-explain",
  "-explain-cyclic",
  "-rewrite"
)
lazy val root = project.in(file(".")).
  enablePlugins(ScalaNativePlugin).
  dependsOn(routing).
  dependsOn(jsonCodec).
  settings(

    // defaults set with common options shown
    nativeConfig ~= { c =>
      c.withLTO(LTO.none) // thin
        .withMode(Mode.debug) // releaseFast
        .withGC(GC.immix)
        .withLinkingOptions(
          c.linkingOptions ++ Seq(
            "-lboost_thread", "-lboost_fiber", "-lboost_context"
          )
        )
        .withCompileOptions(c.compileOptions ++ Seq("-g"))
        //.withCompileOptions(c.compileOptions ++ Seq("-std=c++17"))
        .withClangPP(file("/usr/bin/clang++").toPath)
      //.withClang(file("/usr/bin/clang").toPath)
    },
    appStop := {
      val logger: TaskStreams = streams.value
      val shell: Seq[String] = if (sys.props("os.name").contains("Windows")) Seq("cmd", "/c") else Seq("bash", "-c")
      val cmdGetPid = Seq(
        "ps", "-ef", "|", "grep", name.value, "|", "grep", "-v", "grep", "|", "awk", "'{print $2}'"
      ).mkString(" ")

      //logger.log.info(s"execute: ${cmdGetPid.mkString(" ")}")
      val pid = ((shell :+ cmdGetPid) !!)
      if (pid.nonEmpty) {

        logger.log.info(s"PID=$pid")

        val cmd = Seq(
          "kill", "-s", "9", pid
        ).mkString(" ")

        val result = ((shell :+ cmd) ! logger.log)
        if(result == 0){
          logger.log.success(s"stop app successful")
        } else {
          logger.log.success("stop app failure")
        }
      } else {
        logger.log.info("app is not running")
      }
    },

    showPid := {
      val logger: TaskStreams = streams.value
      val shell: Seq[String] = if (sys.props("os.name").contains("Windows")) Seq("cmd", "/c") else Seq("bash", "-c")
      val cmd = Seq(
        "ps", "-ef", "|", "grep", name.value, "|", "grep", "-v", "grep", "|", "awk", "'{print $2}'"
      ).mkString(" ")

      //logger.log.info(s"execute: ${cmd.mkString(" ")}")
      val pid = (shell :+ cmd) !!

      if(pid.nonEmpty){
        logger.log.info(s"PID=$pid")
      }else{
        logger.log.info(s"pid not found")
      }
    },
    appRestart := {
      val logger: TaskStreams = streams.value
      logger.log.info("app restart..")
      appStop.evaluated
      (Compile / run).evaluated
    }
  )

addCommandAlias("run", "appStart")

