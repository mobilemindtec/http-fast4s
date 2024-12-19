import scala.language.postfixOps
import scala.scalanative.build.*
import scala.sys.process.*

scalaVersion := "3.6.2"
name := "http-fast4s"
organization := "io.http.fast4s"

// set to Debug for compilation details (Info is default)
logLevel := Level.Info
ThisBuild / usePipelining := true

lazy val appStop = inputKey[Unit]("stop app")
lazy val appRestart = inputKey[Unit]("run app")
lazy val showPid = inputKey[Unit]("show app PID")

lazy val microRouter = ProjectRef(file("../micro-router"), "appNative")
lazy val jsonCodec = ProjectRef(file("../json-codec"), "appNative")

scalacOptions ++= Seq(
  "-new-syntax",
  //"-no-indent",
  "-Wvalue-discard",
  "-Wunused:all",
  //"-Werror",
  "-deprecation",
  "-explain",
  "-explain-cyclic",
  "-rewrite",
  "-source:future",
  "-language:experimental.modularity"
)
lazy val root = project.in(file(".")).
  enablePlugins(ScalaNativePlugin).
  dependsOn(microRouter).
  dependsOn(jsonCodec).
  settings(

    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.19" % "test",

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

