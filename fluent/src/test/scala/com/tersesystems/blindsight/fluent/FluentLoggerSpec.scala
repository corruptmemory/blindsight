package com.tersesystems.blindsight.fluent

import java.util.UUID

import com.tersesystems.blindsight.{Arguments, Markers, Message, Statement, ToArguments, ToStatement}
import com.tersesystems.blindsight.fixtures.OneContextPerTest
import net.logstash.logback.argument.StructuredArgument
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import net.logstash.logback.argument.StructuredArguments.{keyValue, kv}
import org.slf4j.MarkerFactory

class FluentLoggerSpec extends AnyWordSpec with Matchers with OneContextPerTest {

  final case class PayloadModel(payloadId: UUID, userSecretToken: String, data: String) {
    override def toString: String = s"PayloadModel(uuid=${payloadId})"
  }

  "work with everything" in {

    implicit val payloadToArguments: ToArguments[PayloadModel] = ToArguments { instance =>
      Arguments(keyValue("uuid", instance.payloadId))
    }

    val underlying = loggerContext.getLogger("logger")
    val fluentLogger: FluentLogger = FluentLogger(underlying)
    fluentLogger.info
      .marker("HELLO")
      .message("User logged out")
      .argument(PayloadModel(UUID.randomUUID(), "secretToken", "data"))
      .cause(new Exception("exception"))
      .logWithPlaceholders()

    val event = listAppender.list.get(0)
    event.getMarker.contains(MarkerFactory.getMarker("HELLO")) must be(true)
    event.getMessage must equal("User logged out  {} {}")
    event.getThrowableProxy.getMessage must equal("exception")
    event.getArgumentArray must ===(Array(kv("name", "steve"), kv("reason", "timeout")))
  }

  "work with exception" in {
    val underlying = loggerContext.getLogger("logger")

    val fluentLogger: FluentLogger = FluentLogger(underlying)
    fluentLogger.info.cause(new Exception("exception")).log()

    val event = listAppender.list.get(0)
    event.getMessage must equal("")
    event.getThrowableProxy.getMessage must equal("exception")
  }

  "work with string arguments only" in {
    val underlying = loggerContext.getLogger("logger")

    val fluentLogger: FluentLogger = FluentLogger(underlying)
    fluentLogger.info.argument("only arguments").logWithPlaceholders()

    val event = listAppender.list.get(0)
    event.getMessage must equal(" {}")
    event.getArgumentArray must ===(Array("only arguments"))
  }

  "work with kv arguments" in {
    val underlying = loggerContext.getLogger("logger")

    val fluentLogger: FluentLogger = FluentLogger(underlying)
    fluentLogger.info.argument(kv("key", "value")).log()

    val event = listAppender.list.get(0)
    event.getMessage must equal("")
    event.getArgumentArray must ===(Array(kv("key", "value")))
  }

  implicit val kvToArguments: ToArguments[StructuredArgument] = ToArguments(Arguments(_))
}
