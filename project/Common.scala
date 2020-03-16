
import Dependencies._
import de.heikoseeberger.sbtheader.HeaderPlugin

import sbt._
import sbt.Keys._

// https://www.scala-sbt.org/1.x/docs/Plugins.html#Creating+an+auto+plugin
object Common extends AutoPlugin {
  override def requires: Plugins = plugins.JvmPlugin && HeaderPlugin
  override def trigger = allRequirements

  override def globalSettings: Seq[Def.Setting[_]] = {
    Seq(
      libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.1" % Test,
      libraryDependencies += logbackClassic % Test,
      libraryDependencies += logstashLogbackEncoder % Test,
      libraryDependencies += scalaTest % Test
    )
  }
}
