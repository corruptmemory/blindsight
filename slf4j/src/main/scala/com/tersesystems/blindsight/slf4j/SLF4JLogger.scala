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

import com.tersesystems.blindsight.api.mixins.{MarkerMixin, ParameterListMixin, PredicateMixin, SourceInfoMixin}
import com.tersesystems.blindsight.api.{Arguments, Markers, ParameterList, ToMarkers, ToMessage}
import org.slf4j.{Logger, Marker}
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

trait SLF4JLogger
    extends SLF4JLoggerAPI[SLF4JLoggerPredicate, SLF4JLoggerMethod]
    with MarkerMixin
    with SourceInfoMixin
    with ParameterListMixin
    with PredicateMixin[SLF4JLoggerPredicate] {
  override type Self <: SLF4JLogger

  def underlying: org.slf4j.Logger

  def onCondition(test: => Boolean): SLF4JLogger
}

object SLF4JLogger {

  abstract class Impl(val underlying: org.slf4j.Logger, val markerState: Markers)
    extends SLF4JLogger {
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

    def method(level: Level): Method = methods(level.ordinal())

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

    override def marker[T: ToMarkers](markerInst: T): Self = {
      val markers = implicitly[ToMarkers[T]].toMarkers(markerInst)
      self(underlying, markerState ++ markers)
    }

    protected def self(underlying: org.slf4j.Logger, markerState: Markers): Self

    override def onCondition(test: => Boolean): Self = new Conditional(test, this)
  }

  class Conditional(test: => Boolean, protected val logger: SLF4JLogger)
    extends SLF4JLogger {
    override type Self      = SLF4JLogger
    override type Method    = SLF4JLoggerMethod
    override type Predicate = logger.Predicate

    def onCondition(test2: => Boolean): Self = new Conditional(test && test2, logger)

    override def isTraceEnabled: Predicate = logger.isTraceEnabled
    override def trace: Method             = new ConditionalLoggerMethod(Level.TRACE, test, logger)

    override def isDebugEnabled: Predicate = logger.isDebugEnabled
    override def debug: Method             = new ConditionalLoggerMethod(Level.DEBUG, test, logger)

    override def isInfoEnabled: Predicate = logger.isInfoEnabled
    override def info: Method             = new ConditionalLoggerMethod(Level.INFO, test, logger)

    override def isWarnEnabled: Predicate = logger.isWarnEnabled
    override def warn: Method             = new ConditionalLoggerMethod(Level.WARN, test, logger)

    override def isErrorEnabled: Predicate = logger.isErrorEnabled
    override def error: Method             = new ConditionalLoggerMethod(Level.ERROR, test, logger)

    override def marker[T: ToMarkers](markerInstance: T): SLF4JLogger = {
      new Conditional(test, logger.marker(markerInstance))
    }

    override def markerState: Markers = logger.markerState
    override def sourceInfoMarker(level: Level, line: Line, file: File, enclosing: Enclosing): Markers = {
      logger.sourceInfoMarker(level, line, file, enclosing)
    }

    override def parameterList(level: Level): ParameterList = logger.parameterList(level)
    override def predicate(level: Level): SLF4JLoggerPredicate = logger.predicate(level)

    override def underlying: Logger = logger.underlying
  }

  class ConditionalLoggerMethod(
                                 val level: Level,
                                 test: => Boolean,
                                 logger: SLF4JLogger with ParameterListMixin
                               ) extends SLF4JLoggerMethod {
    protected val parameterList: ParameterList = logger.parameterList(level)

    override def apply[M: ToMessage](
                                      instance: => M)(implicit line: Line, file: File, enclosing: Enclosing): Unit = if (test) {
      parameterList.message(implicitly[ToMessage[M]].toMessage(instance).toString)
    }

    override def apply[M: ToMessage](instance: => M, args: Arguments)(implicit line: Line,
                                                                      file: File,
                                                                      enclosing: Enclosing): Unit = if (test) {
      parameterList.messageArgs(implicitly[ToMessage[M]].toMessage(instance).toString, args.asArray)
    }

    override def apply[M: ToMessage](instance: => M, arg: Any)(implicit line: Line,
                                                               file: File,
                                                               enclosing: Enclosing): Unit = if (test) {
      parameterList.messageArg1(implicitly[ToMessage[M]].toMessage(instance).toString, arg)
    }

    override def apply[M: ToMessage](instance: => M, arg1: Any, arg2: Any)(
      implicit line: Line,
      file: File,
      enclosing: Enclosing): Unit = if (test) {
      parameterList.messageArg1Arg2(implicitly[ToMessage[M]].toMessage(instance).toString, arg1, arg2)
    }

    override def apply[M: ToMessage](instance: => M, args: Any*)(implicit line: Line,
                                                                 file: File,
                                                                 enclosing: Enclosing): Unit = if (test) {
      parameterList.messageArgs(implicitly[ToMessage[M]].toMessage(instance).toString, args)
    }

    override def apply[M: ToMessage](
                                      marker: => Marker,
                                      instance: => M)(implicit line: Line, file: File, enclosing: Enclosing): Unit = if (test) {
      parameterList.markerMessage(marker, implicitly[ToMessage[M]].toMessage(instance).toString)
    }

    override def apply[M: ToMessage](marker: => Marker, instance: => M, args: Arguments)(
      implicit line: Line,
      file: File,
      enclosing: Enclosing): Unit = if (test) {
      parameterList.markerMessageArgs(marker, implicitly[ToMessage[M]].toMessage(instance).toString, args.asArray)
    }

    override def apply[M: ToMessage](marker: => Marker, instance: => M, arg: Any)(
      implicit line: Line,
      file: File,
      enclosing: Enclosing): Unit = if (test) {
      parameterList.markerMessageArg1(marker, implicitly[ToMessage[M]].toMessage(instance).toString, arg)
    }

    override def apply[M: ToMessage](marker: => Marker, instance: => M, arg1: Any, arg2: Any)(
      implicit line: Line,
      file: File,
      enclosing: Enclosing): Unit = if (test) {
      parameterList.markerMessageArg1Arg2(marker, implicitly[ToMessage[M]].toMessage(instance).toString, arg1, arg2)
    }

    override def apply[M: ToMessage](marker: => Marker, instance: => M, args: Any*)(
      implicit line: Line,
      file: File,
      enclosing: Enclosing): Unit = if (test) {
      parameterList.markerMessageArgs(marker, implicitly[ToMessage[M]].toMessage(instance).toString, args)
    }
  }
}
