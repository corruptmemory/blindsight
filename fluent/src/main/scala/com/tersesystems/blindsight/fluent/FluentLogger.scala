/*
 * Copyright 2020 Terse Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tersesystems.blindsight.fluent

import com.tersesystems.blindsight.api.mixins._
import com.tersesystems.blindsight.api._
import com.tersesystems.blindsight.slf4j._
import org.slf4j.Logger
import org.slf4j.event.Level
import org.slf4j.event.Level._
import sourcecode.{Enclosing, File, Line}

trait FluentLogger
    extends SLF4JLoggerAPI[SLF4JLoggerPredicate, FluentLoggerMethod]
    with PredicateMixin[SLF4JLoggerPredicate]
    with ParameterListMixin
    with MarkerMixin
    with SourceInfoMixin {
  override type Self      = FluentLogger
  override type Method    = FluentLoggerMethod
  override type Predicate = SLF4JLoggerPredicate

  def underlying: org.slf4j.Logger

  def onCondition(test: => Boolean): FluentLogger
}

object FluentLogger {

  class Impl(logger: SLF4JLogger) extends FluentLogger with ParameterListMixin {
    override def marker[T: ToMarkers](markerInstance: T): Self = {
      new Impl(logger.marker(markerInstance))
    }

    override def onCondition(test: => Boolean): FluentLogger = {
      new Conditional(test, this)
    }

    override def isTraceEnabled: Predicate = logger.predicate(TRACE)
    override def trace: Method             = new FluentLoggerMethod.Impl(TRACE, this)

    override def isDebugEnabled: Predicate = logger.predicate(DEBUG)
    override def debug: Method             = new FluentLoggerMethod.Impl(DEBUG, this)

    override def isInfoEnabled: Predicate = logger.predicate(INFO)
    override def info: Method             = new FluentLoggerMethod.Impl(INFO, this)

    override def isWarnEnabled: Predicate = logger.predicate(WARN)
    override def warn: Method             = new FluentLoggerMethod.Impl(WARN, this)

    override def isErrorEnabled: Predicate = logger.predicate(ERROR)
    override def error: Method             = new FluentLoggerMethod.Impl(ERROR, this)

    override def parameterList(level: Level): ParameterList = logger.parameterList(level)
    override def predicate(level: Level): Predicate         = logger.predicate(level)
    override def markerState: Markers                       = logger.markerState

    override def sourceInfoMarker(
        level: Level,
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Markers = logger.sourceInfoMarker(level, line, file, enclosing)

    override def underlying: org.slf4j.Logger = logger.underlying

  }

  class Conditional(test: => Boolean, logger: FluentLogger) extends FluentLogger {
    override type Self      = FluentLogger
    override type Method    = FluentLoggerMethod
    override type Predicate = SLF4JLoggerPredicate

    override def marker[T: ToMarkers](markerInstance: T): Self = {
      new Conditional(test, logger.marker(markerInstance))
    }

    override def onCondition(test2: => Boolean): Self = {
      new Conditional(test && test2, logger)
    }

    override def isTraceEnabled: Predicate = logger.isTraceEnabled
    override def trace: Method             = new ConditionalFluentLoggerMethod(Level.TRACE, test, logger)

    override def isDebugEnabled: Predicate = logger.isDebugEnabled
    override def debug: Method             = new ConditionalFluentLoggerMethod(Level.DEBUG, test, logger)

    override def isInfoEnabled: Predicate = logger.isInfoEnabled
    override def info: Method             = new ConditionalFluentLoggerMethod(Level.INFO, test, logger)

    override def isWarnEnabled: Predicate = logger.isWarnEnabled
    override def warn: Method             = new ConditionalFluentLoggerMethod(Level.WARN, test, logger)

    override def isErrorEnabled: Predicate = logger.isErrorEnabled
    override def error: Method             = new ConditionalFluentLoggerMethod(Level.ERROR, test, logger)

    override def markerState: Markers = logger.markerState

    override def sourceInfoMarker(level: Level, line: Line, file: File, enclosing: Enclosing): Markers = {
      logger.sourceInfoMarker(level, line, file, enclosing)
    }

    override def parameterList(level: Level): ParameterList = logger.parameterList(level)

    override def predicate(level: Level): SLF4JLoggerPredicate = logger.predicate(level)

    override def underlying: Logger = logger.underlying
  }

  class ConditionalFluentLoggerMethod(level: Level, test: => Boolean, logger: FluentLogger)
    extends FluentLoggerMethod.Impl(level, logger) {

    override def apply[T: ToStatement](
                                        instance: => T
                                      )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (test) {
        val statement = implicitly[ToStatement[T]].toStatement(instance)
        logger.parameterList(level).executeStatement(statement)
      }
    }
  }

}
