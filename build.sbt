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

val disableDocs = Seq[Setting[_]](
  sources in (Compile, doc) := Seq.empty,
  publishArtifact in (Compile, packageDoc) := false
)

val disablePublishing = Seq[Setting[_]](
  publishArtifact := false,
  skip in publish := true
)

// sbt ghpagesPushSite to publish to ghpages
// previewAuto to see the site in action.
// https://www.scala-sbt.org/sbt-site/getting-started.html#previewing-the-site
lazy val docs = (project in file("docs"))
  .enablePlugins(ParadoxPlugin, ParadoxSitePlugin, GhpagesPlugin)
  .settings(
    scmInfo := Some(ScmInfo(url("https://github.com/tersesystems/blindsight"), "scm:git:git@github.com:tersesystems/blindsight.git")),
    git.remoteRepo := scmInfo.value.get.connection.replace("scm:git:", ""),

    paradoxTheme := Some(builtinParadoxTheme("generic")),
    mappings in makeSite ++= Seq(
      file("LICENSE") -> "LICENSE"
    )
  ).settings(disablePublishing)

lazy val fixtures = (project in file("fixtures"))
  .settings(
    libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.1" % Test,
    libraryDependencies += logbackClassic           % Test,
    libraryDependencies += logstashLogbackEncoder   % Test,
    libraryDependencies += scalaTest                % Test
  ).settings(disablePublishing).settings(disableDocs)

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
  .dependsOn(api, fixtures % "test->test")

lazy val semantic = (project in file("semantic"))
  .settings(
    name := "blindsight-semantic",
    libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.1" % Test,
    libraryDependencies += logbackClassic           % Test,
    libraryDependencies += logstashLogbackEncoder   % Test,
    libraryDependencies += scalaTest                % Test
  )
  .dependsOn(slf4j, api)
  .dependsOn(fixtures % "test->test")

lazy val fluent = (project in file("fluent"))
  .settings(
    name := "blindsight-fluent",
    libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.1" % Test,
    libraryDependencies += logbackClassic           % Test,
    libraryDependencies += logstashLogbackEncoder   % Test,
    libraryDependencies += scalaTest                % Test
  )
  .dependsOn(slf4j, api)
  .dependsOn(fixtures % "test->test")

// API that provides a logger with everything
lazy val all = (project in file("all")).settings(
  name := "blindsight"
).dependsOn(api, slf4j, semantic, fluent)

// serviceloader implementation with logback dependencies
lazy val logback = (project in file("logback"))
  .settings(
    name := "blindsight-logback",
    libraryDependencies += logbackClassic
  ).dependsOn(all)

lazy val logstash = (project in file("logstash"))
  .settings(
    name := "blindsight-logstash",
    libraryDependencies += jacksonModuleScala,
    libraryDependencies += logstashLogbackEncoder
  )
  .dependsOn(logback, fixtures % "test->test")

// serviceloader implementation with log4j2 dependencies
lazy val log4j2 = (project in file("log4j2"))
  .settings(
    name := "blindsight-log4j2",
    libraryDependencies += "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.13.1"
  ).dependsOn(all)

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
  )
  .dependsOn(logstash).settings(disablePublishing).settings(disableDocs)

lazy val root = (project in file("."))
  .settings(
    name := "blindsight-root"
  ).settings(disableDocs).settings(disablePublishing)
  .aggregate(docs, fixtures, api, slf4j, semantic, fluent, logstash, all, logback, log4j2, example)
