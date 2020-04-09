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

package com.tersesystems.blindsight.slf4j

import com.tersesystems.blindsight.api._
import com.tersesystems.blindsight.api.mixins._
import org.slf4j._
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

trait SLF4JLogger
    extends SLF4JLoggerAPI[SLF4JLoggerPredicate, SLF4JLoggerMethod]
    with MarkerMixin
    with UnderlyingMixin
    with OnConditionMixin {
  override type Self <: SLF4JLogger
}

/** extended service level interface */
trait ExtendedSLF4JLogger
  extends SLF4JLogger
    with SourceInfoMixin
    with ParameterListMixin
    with PredicateMixin[SLF4JLoggerPredicate] {
  def method(level: Level): SLF4JLoggerMethod
}

object SLF4JLogger {

  abstract class Impl(val underlying: org.slf4j.Logger, val markers: Markers)
      extends ExtendedSLF4JLogger {
    override type Self      = SLF4JLogger
    override type Method    = SLF4JLoggerMethod
    override type Predicate = SLF4JLoggerPredicate

    protected val parameterLists: Seq[ParameterList] = ParameterList.lists(this.underlying)

    private val methods = Array(
      error,
      warn,
      info,
      debug,
      trace
    )

    private val predicates = Array(
      isErrorEnabled,
      isWarnEnabled,
      isInfoEnabled,
      isDebugEnabled,
      isTraceEnabled
    )

    override def method(level: Level): Method = methods(level.ordinal())

    override def predicate(level: Level): Predicate = predicates(level.ordinal())

    @inline
    def parameterList(level: Level): ParameterList = parameterLists(level.ordinal)

    override def isTraceEnabled: Predicate = new SLF4JLoggerPredicate.Impl(Level.TRACE, this)
    override def trace: Method             = new SLF4JLoggerMethod.Impl(Level.TRACE, this)

    override def isDebugEnabled: Predicate = new SLF4JLoggerPredicate.Impl(Level.DEBUG, this)
    override def debug: Method             = new SLF4JLoggerMethod.Impl(Level.DEBUG, this)

    override def isInfoEnabled: Predicate = new SLF4JLoggerPredicate.Impl(Level.INFO, this)
    override def info: Method             = new SLF4JLoggerMethod.Impl(Level.INFO, this)

    override def isWarnEnabled: Predicate = new SLF4JLoggerPredicate.Impl(Level.WARN, this)
    override def warn: Method             = new SLF4JLoggerMethod.Impl(Level.WARN, this)

    override def isErrorEnabled: Predicate = new SLF4JLoggerPredicate.Impl(Level.ERROR, this)
    override def error: Method             = new SLF4JLoggerMethod.Impl(Level.ERROR, this)

    override def withMarker[T: ToMarkers](markerInst: T): Self = {
      val markers = implicitly[ToMarkers[T]].toMarkers(markerInst)
      self(underlying, markers ++ markers)
    }

    protected def self(underlying: org.slf4j.Logger, markerState: Markers): Self

    override def onCondition(test: => Boolean): Self = new Conditional(test, this)
  }

  class Conditional(test: => Boolean, protected val logger: ExtendedSLF4JLogger)
    extends ExtendedSLF4JLogger {
    override type Self = SLF4JLogger
    override type Method = SLF4JLoggerMethod
    override type Predicate = logger.Predicate

    override def onCondition(test2: => Boolean): Self = new Conditional(test && test2, logger)

    override def withMarker[T: ToMarkers](markerInstance: T): SLF4JLogger = {
      new Conditional(test, logger.withMarker(markerInstance).asInstanceOf[ExtendedSLF4JLogger])
    }

    override def isTraceEnabled: Predicate = logger.isTraceEnabled
    override def trace: Method             = new SLF4JLoggerMethod.Conditional(Level.TRACE, test, logger)

    override def isDebugEnabled: Predicate = logger.isDebugEnabled
    override def debug: Method             = new SLF4JLoggerMethod.Conditional(Level.DEBUG, test, logger)

    override def isInfoEnabled: Predicate = logger.isInfoEnabled
    override def info: Method             = new SLF4JLoggerMethod.Conditional(Level.INFO, test, logger)

    override def isWarnEnabled: Predicate = logger.isWarnEnabled
    override def warn: Method             = new SLF4JLoggerMethod.Conditional(Level.WARN, test, logger)

    override def isErrorEnabled: Predicate = logger.isErrorEnabled
    override def error: Method             = new SLF4JLoggerMethod.Conditional(Level.ERROR, test, logger)

    override def markers: Markers = logger.markers

    override def sourceInfoMarker(
                                   level: Level,
                                   line: Line,
                                   file: File,
                                   enclosing: Enclosing
                                 ): Markers = {
      logger.sourceInfoMarker(level, line, file, enclosing)
    }

    override def parameterList(level: Level): ParameterList = logger.parameterList(level)

    override def predicate(level: Level): SLF4JLoggerPredicate = logger.predicate(level)

    override def underlying: Logger = logger.underlying

    override def method(level: Level): SLF4JLoggerMethod = logger.method(level)
  }

}
