package com.tersesystems.blindsight.semantic

import java.util.UUID

import com.tersesystems.blindsight.{Arguments, Markers, Message, Statement, ToMarkers, ToMessage, ToStatement}
import com.tersesystems.blindsight.fixtures.OneContextPerTest
import net.logstash.logback.argument.StructuredArguments
import net.logstash.logback.argument.StructuredArguments.{keyValue, _}
import net.logstash.logback.marker.{Markers => LogstashMarkers}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class SemanticLoggerSpec extends AnyWordSpec with Matchers with OneContextPerTest {

  "a logger" when {

    "run against statement" in {
      implicit val payloadToArguments: ToStatement[PayloadModel] = ToStatement { instance =>
        Statement(
          Markers.empty,
          Message("payloadModel:"),
          Arguments((keyValue("uuid", instance.payloadId))),
          None
        )
      }

      val underlying = loggerContext.getLogger("testing")
      val payloadLogger: SemanticLogger[PayloadModel] = SemanticLogger(underlying)
      val uuid = UUID.randomUUID()
      payloadLogger.info(PayloadModel(uuid, "1234", "data"))

      val event = listAppender.list.get(0)
      event.getMessage must be("payloadModel:")
      event.getArgumentArray must equal(Array(keyValue("uuid", uuid)))
    }


    "run against a constructed statement" in {
      implicit val tupleStringToMarkers: ToMarkers[(String, String)] = ToMarkers { case (k, v) =>
        Markers(LogstashMarkers.append(k, v))
      }

      implicit val payloadToArguments: ToStatement[PayloadModel] = ToStatement { instance =>
        // XXX Make a builder out of Statement
        Statement(
          markers = Markers("secretToken" -> instance.userSecretToken),
          message = Message("herp" -> "derp"),
          arguments = Arguments(StructuredArguments.kv("uuid", instance.payloadId)),
          None
        )
      }

      val underlying = loggerContext.getLogger("testing")
      val payloadLogger: SemanticLogger[PayloadModel] = SemanticLogger(underlying)
      val uuid = UUID.randomUUID()
      payloadLogger.info(PayloadModel(uuid, "1234", "data"))

      val event = listAppender.list.get(0)
      event.getMessage must be("herp=derp")
      event.getArgumentArray must equal(Array(keyValue("uuid", uuid)))
    }
  }
}


final case class PayloadModel(payloadId: UUID, userSecretToken: String, data: String) {
  override def toString: String = s"PayloadModel(uuid=${payloadId})"
}


