package com.tersesystems.blindsight.api.fluent

import java.util.UUID

import com.tersesystems.blindsight.api.{Arguments, Markers, ToArguments}
import com.tersesystems.blindsight.fixtures.OneContextPerTest
import com.tersesystems.blindsight.fluent.FluentLogger
import com.tersesystems.blindsight.slf4j.SLF4JLogger
import com.tersesystems.blindsight.slf4j.SLF4JLogger.Impl
import net.logstash.logback.argument.StructuredArgument
import net.logstash.logback.argument.StructuredArguments.{keyValue, kv}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.slf4j.event.Level
import org.slf4j.{Logger, MarkerFactory}
import sourcecode.{Enclosing, File, Line}

class FluentLoggerSpec extends AnyWordSpec with Matchers with OneContextPerTest {

  protected class NoSourceSLF4JLogger(underlying: org.slf4j.Logger, markers: Markers = Markers.empty) extends Impl(underlying, markers) {
    override protected def self(underlying: Logger, markerState: Markers): SLF4JLogger = {
      new NoSourceSLF4JLogger(underlying, markerState)
    }

    override def sourceInfoMarker(level: Level, line: Line, file: File, enclosing: Enclosing): Markers = Markers.empty
  }

  final case class PayloadModel(payloadId: UUID, userSecretToken: String, data: String) {
    override def toString: String = s"PayloadModel(uuid=$payloadId)"
  }

  "work with everything" in {

    implicit val payloadToArguments: ToArguments[PayloadModel] = ToArguments { instance =>
      Arguments(keyValue("uuid", instance.payloadId))
    }

    val underlying                 = loggerContext.getLogger("logger")
    val fluentLogger: FluentLogger = new FluentLogger.Impl(new NoSourceSLF4JLogger(underlying))
    val uuid                       = UUID.randomUUID()
    fluentLogger.info
      .marker("HELLO")
      .message("User logged out")
      .argument(PayloadModel(uuid, "secretToken", "data"))
      .cause(new Exception("exception"))
      .logWithPlaceholders()

    val event = listAppender.list.get(0)
    event.getMarker.contains(MarkerFactory.getMarker("HELLO")) must be(true)
    event.getMessage must equal("User logged out  {}")
    event.getThrowableProxy.getMessage must equal("exception")
    event.getArgumentArray must ===(Array(kv("uuid", uuid)))
  }

  "work with exception" in {
    val underlying = loggerContext.getLogger("logger")

    val fluentLogger: FluentLogger = new FluentLogger.Impl(new NoSourceSLF4JLogger(underlying))

    fluentLogger.info.cause(new Exception("exception")).log()

    val event = listAppender.list.get(0)
    event.getMessage must equal("")
    event.getThrowableProxy.getMessage must equal("exception")
  }

  "work with string arguments only" in {
    val underlying = loggerContext.getLogger("logger")

    val fluentLogger: FluentLogger = new FluentLogger.Impl(new NoSourceSLF4JLogger(underlying))
    fluentLogger.info.argument("only arguments").logWithPlaceholders()

    val event = listAppender.list.get(0)
    event.getMessage must equal(" {}")
    event.getArgumentArray must ===(Array("only arguments"))
  }

  "work with kv arguments" in {
    val underlying = loggerContext.getLogger("logger")

    val fluentLogger: FluentLogger = new FluentLogger.Impl(new NoSourceSLF4JLogger(underlying))
    fluentLogger.info.argument(kv("key", "value")).log()

    val event = listAppender.list.get(0)
    event.getMessage must equal("")
    event.getArgumentArray must ===(Array(kv("key", "value")))
  }

  implicit val kvToArguments: ToArguments[StructuredArgument] = ToArguments(Arguments(_))
}
