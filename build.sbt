import Dependencies._
import sbt.Keys.libraryDependencies

lazy val scala213 = "2.13.1"
ThisBuild / scalaVersion := scala213

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.tersesystems.blindsight"
ThisBuild / organizationName := "Terse Systems"

ThisBuild / startYear := Some(2020)
ThisBuild / licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / headerLicense := None

// previewAuto to see the site in action.
// https://www.scala-sbt.org/sbt-site/getting-started.html#previewing-the-site
lazy val docs = (project in file("docs"))
  .enablePlugins(ParadoxPlugin, ParadoxSitePlugin)
  .settings(
    paradoxTheme := Some(builtinParadoxTheme("generic")),
    mappings in makeSite ++= Seq(
      file("LICENSE") -> "LICENSE"
    )
  )

lazy val fixtures = (project in file("fixtures"))
  .settings(
    libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.1" % Test,
    libraryDependencies += logbackClassic           % Test,
    libraryDependencies += logstashLogbackEncoder   % Test,
    libraryDependencies += scalaTest                % Test
  )

lazy val api = (project in file("api")).settings(
  name := "blindsight-api",
  libraryDependencies += slf4jApi,
  libraryDependencies += sourcecode
)

lazy val slf4j = (project in file("slf4j"))
  .settings(
    name := "blindsight-slf4j",
    libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.1" % Test,
    libraryDependencies += logbackClassic           % Test,
    libraryDependencies += logstashLogbackEncoder   % Test,
    libraryDependencies += scalaTest                % Test
  )
  .dependsOn(api, fixtures % "compile->compile;test->test")

lazy val semantic = (project in file("semantic"))
  .settings(
    name := "blindsight-semantic",
    libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.1" % Test,
    libraryDependencies += logbackClassic           % Test,
    libraryDependencies += logstashLogbackEncoder   % Test,
    libraryDependencies += scalaTest                % Test
  )
  .dependsOn(slf4j, api)
  .dependsOn(fixtures % "compile->compile;test->test")

lazy val fluent = (project in file("fluent"))
  .settings(
    name := "blindsight-fluent",
    libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.1" % Test,
    libraryDependencies += logbackClassic           % Test,
    libraryDependencies += logstashLogbackEncoder   % Test,
    libraryDependencies += scalaTest                % Test
  )
  .dependsOn(slf4j, api)
  .dependsOn(fixtures % "compile->compile;test->test")

lazy val logstash = (project in file("logstash"))
  .settings(
    name := "blindsight-logstash",
    libraryDependencies += jacksonModuleScala,
    libraryDependencies += logbackClassic,
    libraryDependencies += logstashLogbackEncoder
  )
  .dependsOn(api)

lazy val example = (project in file("example"))
  .settings(
    name := "example",
    resolvers += Resolver.bintrayRepo("tersesystems", "maven"),
    resolvers += Resolver.mavenLocal,
    libraryDependencies += "com.casualmiracles" %% "treelog-cats" % "1.6.0",
    libraryDependencies += janino,
    libraryDependencies += jansi,
    libraryDependencies += logbackBudget,
    libraryDependencies += logbackTurboMarker,
    libraryDependencies += logbackTypesafeConfig,
    libraryDependencies += logbackExceptionMapping,
    libraryDependencies += logbackExceptionMappingProvider,
    libraryDependencies += logbackUniqueId,
    libraryDependencies += logbackTracing,
    libraryDependencies += logbackClassic
  )
  .dependsOn(api, semantic, fluent, logstash)

lazy val root = (project in file("."))
  .settings(
    name := "blindsight-root"
  )
  .aggregate(docs, fixtures, api, slf4j, semantic, fluent, logstash, example)
