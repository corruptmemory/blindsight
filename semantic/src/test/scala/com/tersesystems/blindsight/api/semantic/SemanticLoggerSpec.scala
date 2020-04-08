package com.tersesystems.blindsight.api.semantic

import java.util.UUID

import com.tersesystems.blindsight.api._
import com.tersesystems.blindsight.fixtures.OneContextPerTest
import com.tersesystems.blindsight.semantic.SemanticLogger
import com.tersesystems.blindsight.slf4j.SLF4JLogger
import com.tersesystems.blindsight.slf4j.SLF4JLogger.Impl
import net.logstash.logback.argument.StructuredArguments
import net.logstash.logback.argument.StructuredArguments.keyValue
import net.logstash.logback.marker.{Markers => LogstashMarkers}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.slf4j.Logger
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

class SemanticLoggerSpec extends AnyWordSpec with Matchers with OneContextPerTest {

  "a logger" when {

    "run against statement" in {
      implicit val payloadToArguments: ToStatement[PayloadModel] = ToStatement { instance =>
        Statement(
          Markers.empty,
          Message("payloadModel:"),
          Arguments(keyValue("uuid", instance.payloadId)),
          None
        )
      }

      val underlying                                  = loggerContext.getLogger("testing")
      val payloadLogger: SemanticLogger[PayloadModel] = new SemanticLogger.Impl[PayloadModel](new NoSourceSLF4JLogger(underlying))
      val uuid                                        = UUID.randomUUID()
      payloadLogger.info(PayloadModel(uuid, "1234", "data"))

      val event = listAppender.list.get(0)
      event.getMessage must be("payloadModel:")
      event.getArgumentArray must equal(Array(keyValue("uuid", uuid)))
    }

    "run against a constructed statement" in {
      implicit val tupleStringToMarkers: ToMarkers[(String, String)] = ToMarkers {
        case (k, v) =>
          Markers(LogstashMarkers.append(k, v))
      }

      implicit val payloadToArguments: ToStatement[PayloadModel] = ToStatement { instance =>
        // XXX Make a builder out of Statement
        Statement(
          markers = Markers("secretToken" -> instance.userSecretToken),
          message = Message("herp"        -> "derp"),
          arguments = Arguments(StructuredArguments.kv("uuid", instance.payloadId)),
          None
        )
      }

      val underlying                                  = loggerContext.getLogger("testing")
      val payloadLogger: SemanticLogger[PayloadModel] = new SemanticLogger.Impl[PayloadModel](new NoSourceSLF4JLogger(underlying))
      val uuid                                        = UUID.randomUUID()
      payloadLogger.info(PayloadModel(uuid, "1234", "data"))

      val event = listAppender.list.get(0)
      event.getMessage must be("herp=derp")
      event.getArgumentArray must equal(Array(keyValue("uuid", uuid)))
    }
  }
}

final case class PayloadModel(payloadId: UUID, userSecretToken: String, data: String) {
  override def toString: String = s"PayloadModel(uuid=$payloadId)"
}

protected class NoSourceSLF4JLogger(underlying: org.slf4j.Logger) extends Impl(underlying, Markers.empty) {
  override protected def self(underlying: Logger, markerState: Markers): SLF4JLogger = this

  override def sourceInfoMarker(level: Level, line: Line, file: File, enclosing: Enclosing): Markers = Markers.empty
}
