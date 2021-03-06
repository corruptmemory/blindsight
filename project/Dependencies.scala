import sbt._

object Dependencies {
  val scalaTest = "org.scalatest" %% "scalatest" % "3.1.1"
  val scalamock = "org.scalamock" %% "scalamock" % "4.4.0"

  val terseLogback = "0.16.0"

  lazy val slf4jApi = "org.slf4j" % "slf4j-api" % "1.7.30"

  lazy val sourcecode         = "com.lihaoyi"                  %% "sourcecode"           % "0.1.9"
  lazy val jacksonModuleScala = "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.10.3"
  lazy val janino             = "org.codehaus.janino"          % "janino"                % "3.0.11"
  lazy val jansi              = "org.fusesource.jansi"         % "jansi"                 % "1.17.1"
  lazy val logbackBudget      = "com.tersesystems.logback"     % "logback-budget"        % terseLogback
  lazy val logbackTurboMarker = "com.tersesystems.logback"     % "logback-turbomarker"   % terseLogback
  lazy val logbackTypesafeConfig =
    "com.tersesystems.logback" % "logback-typesafe-config" % terseLogback
  lazy val logbackExceptionMapping =
    "com.tersesystems.logback" % "logback-exception-mapping" % terseLogback
  lazy val logbackExceptionMappingProvider =
    "com.tersesystems.logback" % "logback-exception-mapping-providers" % terseLogback
  lazy val logbackUniqueId        = "com.tersesystems.logback" % "logback-uniqueid-appender" % terseLogback
  lazy val logbackTracing         = "com.tersesystems.logback" % "logback-tracing"           % terseLogback
  lazy val logbackClassic         = "ch.qos.logback"           % "logback-classic"           % "1.2.3"
  lazy val logstashLogbackEncoder = "net.logstash.logback"     % "logstash-logback-encoder"  % "6.3"
}
