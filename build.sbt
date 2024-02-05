import scala.scalanative.build._

scalaVersion := "3.3.1"
name := "http-native"
enablePlugins(ScalaNativePlugin)

// set to Debug for compilation details (Info is default)
logLevel := Level.Info

// import to add Scala Native options
import scala.scalanative.build._

// defaults set with common options shown
nativeConfig ~= { c =>
  c.withLTO(LTO.none) // thin
    .withMode(Mode.debug) // releaseFast
    .withGC(GC.immix)
    .withLinkingOptions(
      c.linkingOptions ++ Seq(
        "-lboost_thread", "-lboost_fiber", "-lboost_context", "-std=c++17"
      )
    )
    //.withCompileOptions(c.compileOptions ++ Seq("-v"))
    //.withCompileOptions(c.compileOptions ++ Seq("-std=c++17"))
    .withClangPP(file("/usr/bin/clang++").toPath)
    //.withClang(file("/usr/bin/clang").toPath)
}
