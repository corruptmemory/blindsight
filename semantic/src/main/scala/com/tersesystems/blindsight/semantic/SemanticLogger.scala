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

package com.tersesystems.blindsight.semantic

import com.tersesystems.blindsight.api._
import com.tersesystems.blindsight.api.mixins._
import com.tersesystems.blindsight.slf4j.{ExtendedSLF4JLogger, SLF4JLoggerPredicate}
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

/**
 * This trait defines an SLF4J compatible logger with all five levels of logging.
 */
trait SemanticLogger[StatementType]
    extends SemanticLoggerAPI[StatementType, SLF4JLoggerPredicate, SemanticLoggerMethod]
      with SemanticMarkerMixin[StatementType]
      with SemanticRefineMixin[StatementType] {
  type Self[T] = SemanticLogger[T]

  def onCondition(test: => Boolean): Self[StatementType]
}

// An extended trait with hooks for implementing methods and predicates.
trait ExtendedSemanticLogger[StatementType]
  extends SemanticLogger[StatementType]
    with PredicateMixin[SLF4JLoggerPredicate]
    with UnderlyingMixin
    with ParameterListMixin
    with SourceInfoMixin

object SemanticLogger {

  class Impl[StatementType](protected val logger: ExtendedSLF4JLogger)
      extends ExtendedSemanticLogger[StatementType]
      with SourceInfoMixin {

    private val methods: Array[Method[StatementType]] = Array(
      error,
      warn,
      info,
      debug,
      trace
    )

    private val predicates: Array[Predicate] = Array(
      isErrorEnabled,
      isWarnEnabled,
      isInfoEnabled,
      isDebugEnabled,
      isTraceEnabled
    )

    def method(level: Level): Method[StatementType] = methods(level.ordinal())

    def predicate(level: Level): Predicate = predicates(level.ordinal())

    def parameterList(level: Level): ParameterList = logger.parameterList(level)

    override def marker[T: ToMarkers](markerInst: T): Self[StatementType] = {
      val markers = implicitly[ToMarkers[T]].toMarkers(markerInst)
      new Impl[StatementType](logger.withMarker(markers).asInstanceOf[ExtendedSLF4JLogger])
    }

    override def refine[T <: StatementType: ToStatement]: Self[T] = new Impl[T](logger)

    override def onCondition(test: => Boolean): Self[StatementType] = {
      new Conditional(test, this)
    }

    override def sourceInfoMarker(
        level: Level,
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Markers = logger.sourceInfoMarker(level, line, file, enclosing)

    override def isTraceEnabled: Predicate    = logger.predicate(Level.TRACE)
    override def trace: Method[StatementType] = new SemanticLoggerMethod.Impl(Level.TRACE, this)

    override def isDebugEnabled: Predicate    = logger.predicate(Level.DEBUG)
    override def debug: Method[StatementType] = new SemanticLoggerMethod.Impl(Level.DEBUG, this)

    override def isInfoEnabled: Predicate    = logger.predicate(Level.INFO)
    override def info: Method[StatementType] = new SemanticLoggerMethod.Impl(Level.INFO, this)

    override def isWarnEnabled: Predicate    = logger.predicate(Level.WARN)
    override def warn: Method[StatementType] = new SemanticLoggerMethod.Impl(Level.WARN, this)

    override def isErrorEnabled: Predicate    = logger.predicate(Level.ERROR)
    override def error: Method[StatementType] = new SemanticLoggerMethod.Impl(Level.ERROR, this)

    override def markerState: Markers = logger.markers

    override def underlying: org.slf4j.Logger = logger.underlying
  }

  class Conditional[StatementType](test: => Boolean, logger: ExtendedSemanticLogger[StatementType])
      extends ExtendedSemanticLogger[StatementType] {
    override type Self[T]   = SemanticLogger[T]
    override type Method[T] = SemanticLoggerMethod[T]
    override type Predicate = SLF4JLoggerPredicate

    override def marker[T: ToMarkers](markerInstance: T): Self[StatementType] = {
      new Conditional(
        test,
        logger.marker(markerInstance).asInstanceOf[ExtendedSemanticLogger[StatementType]]
      )
    }

    override def refine[T <: StatementType: ToStatement]: Self[T] = {
      new Conditional[T](test, logger.asInstanceOf[ExtendedSemanticLogger[T]])
    }

    override def onCondition(test2: => Boolean): Self[StatementType] = {
      new Conditional(test && test2, logger)
    }

    override def isTraceEnabled: Predicate = logger.isTraceEnabled
    override def trace: Method[StatementType] =
      new SemanticLoggerMethod.Conditional(Level.TRACE, test, logger)

    override def isDebugEnabled: Predicate = logger.isDebugEnabled
    override def debug: Method[StatementType] =
      new SemanticLoggerMethod.Conditional(Level.DEBUG, test, logger)

    override def isInfoEnabled: Predicate = logger.isInfoEnabled
    override def info: Method[StatementType] =
      new SemanticLoggerMethod.Conditional(Level.INFO, test, logger)

    override def isWarnEnabled: Predicate = logger.isWarnEnabled
    override def warn: Method[StatementType] =
      new SemanticLoggerMethod.Conditional(Level.WARN, test, logger)

    override def isErrorEnabled: Predicate = logger.isErrorEnabled
    override def error: Method[StatementType] =
      new SemanticLoggerMethod.Conditional(Level.ERROR, test, logger)

    override def markerState: Markers = logger.markerState

    override def sourceInfoMarker(
        level: Level,
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Markers = {
      logger.sourceInfoMarker(level, line, file, enclosing)
    }

    override def predicate(level: Level): SLF4JLoggerPredicate = logger.predicate(level)

    override def underlying: org.slf4j.Logger = logger.underlying

    override def parameterList(level: Level): ParameterList = logger.parameterList(level)
  }
}
