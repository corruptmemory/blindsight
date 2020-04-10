package com.tersesystems.blindsight.logstash

import com.tersesystems.blindsight.api.{Message, ToStatement}
import com.tersesystems.blindsight.fixtures.OneContextPerTest
import com.tersesystems.blindsight.fluent.FluentLogger
import com.tersesystems.blindsight.logstash.Implicits._
import com.tersesystems.blindsight.semantic.SemanticLogger
import com.tersesystems.blindsight.{Logger, LoggerFactory}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class LogstashLoggerSpec extends AnyWordSpec with Matchers with OneContextPerTest {

  "logger" should {

    "work" in {

      val underlying: org.slf4j.Logger = loggerContext.getLogger(this.getClass)
      val logger: Logger = LoggerFactory.getLogger(underlying)
      logger.info("this is an SLF4J message")
      logger.info("a" -> "b")

      val fluentLogger: FluentLogger = logger.fluent
      fluentLogger.info.message("this is a fluent message").log()

      val semanticLogger: SemanticLogger[Message] = logger.refine[Message]
      semanticLogger.info(Message("this is a semantic message"))

      logger.onCondition(1 == 0).info("this is a conditional message")

      logger.withMarker("a" -> "b").info("I have a marker")
    }
  }

  implicit val messageToStatement: ToStatement[Message] = ToStatement { message =>
    message.toStatement
  }

}
