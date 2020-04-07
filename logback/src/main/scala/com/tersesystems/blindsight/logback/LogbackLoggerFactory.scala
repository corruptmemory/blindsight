package com.tersesystems.blindsight.logback

import com.tersesystems.blindsight.{Logger, LoggerFactory, fluent}
import com.tersesystems.blindsight.api._
import com.tersesystems.blindsight.fluent.FluentLogger
import com.tersesystems.blindsight.slf4j._
import com.tersesystems.blindsight.logstash.LogstashSourceInfoMixin
import com.tersesystems.blindsight.semantic.SemanticLogger
import com.tersesystems.blindsight.slf4j.SLF4JLogger.{Conditional, Impl}
import org.slf4j
import org.slf4j.event.Level

class LogbackLoggerFactory extends LoggerFactory {
  override def getLogger[T: LoggerResolver](instance: T): Logger = {
    val underlying = implicitly[LoggerResolver[T]].resolveLogger(instance)
    new LogbackLogger(new LogbackSLF4JLogger(underlying, Markers.empty))
  }

  class LogbackLogger(protected val logger: SLF4JLogger) extends Logger
    with SLF4JLoggerAPI.Proxy[SLF4JLoggerPredicate, SLF4JLoggerMethod]
    with LogstashSourceInfoMixin {
    override type Parent = SLF4JLogger
    override type Self   = Logger

    override def fluent: FluentLogger = {
      new FluentLogger.Impl(logger)
    }

    override def refine[MessageType]: SemanticLogger[MessageType] = {
      new SemanticLogger.Impl[MessageType](logger)
    }

    override def onCondition(test: => Boolean): Self = {
      new LogbackLogger(logger.onCondition(test))
    }

    override def marker[T: ToMarkers](markerInstance: T): Self =
      new LogbackLogger(logger.marker(markerInstance))

    override def markerState: Markers = logger.markerState

    override def parameterList(level: Level): ParameterList = logger.parameterList(level)

    override def predicate(level: Level): SLF4JLoggerPredicate = logger.predicate(level)

    override def underlying: org.slf4j.Logger = logger.underlying
  }

  class LogbackSLF4JLogger(underlying: org.slf4j.Logger, markers: Markers) extends SLF4JLogger.Impl(underlying, markers)
    with LogstashSourceInfoMixin {

    override def onCondition(test: => Boolean): Self = {
      new Conditional(test, this)
    }

    override def marker[T: ToMarkers](markerInst: T): Self = {
      val markers = implicitly[ToMarkers[T]].toMarkers(markerInst)
      self(underlying, markerState ++ markers)
    }

    override protected def self(underlying: slf4j.Logger, markerState: Markers): SLF4JLogger = {
      new LogbackSLF4JLogger(underlying, markerState)
    }
  }
}
