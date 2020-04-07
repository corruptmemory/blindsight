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

import com.tersesystems.blindsight.api.mixins.{ParameterListMixin, PredicateMixin, SemanticMarkerMixin, SourceInfoMixin}
import com.tersesystems.blindsight.api.{Markers, ParameterList, ToMarkers, ToStatement}
import com.tersesystems.blindsight.slf4j.{SLF4JLogger, SLF4JLoggerPredicate}
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

/**
 * This trait defines an SLF4J compatible logger with all five levels of logging.
 */
trait SemanticLogger[MessageType]
    extends SemanticLoggerAPI[MessageType, SLF4JLoggerPredicate, SemanticLoggerMethod]
    with PredicateMixin[SLF4JLoggerPredicate]
    with SemanticMarkerMixin[MessageType]
    with SemanticRefineMixin[MessageType]
    with ParameterListMixin
    with SourceInfoMixin {
  type Self[T] = SemanticLogger[T]
}

object SemanticLogger {

  class Impl[MessageType](protected val logger: SLF4JLogger)
      extends SemanticLogger[MessageType]
      with SourceInfoMixin {

    private val methods: Array[Method[MessageType]] = Array(
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

    def method(level: Level): Method[MessageType] = methods(level.ordinal())

    def predicate(level: Level): Predicate = predicates(level.ordinal())

    def parameterList(level: Level): ParameterList = logger.parameterList(level)

    override def marker[T: ToMarkers](markerInst: T): Self[MessageType] = {
      val markers = implicitly[ToMarkers[T]].toMarkers(markerInst)
      new Impl[MessageType](logger.marker(markers))
    }

    override def refine[T <: MessageType: ToStatement]: Self[T] = new Impl[T](logger)

    override def sourceInfoMarker(
        level: Level,
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Markers = logger.sourceInfoMarker(level, line, file, enclosing)

    override def isTraceEnabled: Predicate = logger.predicate(Level.TRACE)
    override def trace: Method[MessageType]   = new SemanticLoggerMethod.Impl(Level.TRACE, this)

    override def isDebugEnabled: Predicate = logger.predicate(Level.DEBUG)
    override def debug: Method[MessageType]   = new SemanticLoggerMethod.Impl(Level.DEBUG, this)

    override def isInfoEnabled: Predicate = logger.predicate(Level.INFO)
    override def info: Method[MessageType]   = new SemanticLoggerMethod.Impl(Level.INFO, this)

    override def isWarnEnabled: Predicate = logger.predicate(Level.WARN)
    override def warn: Method[MessageType]   = new SemanticLoggerMethod.Impl(Level.WARN, this)

    override def isErrorEnabled: Predicate = logger.predicate(Level.ERROR)
    override def error: Method[MessageType]   = new SemanticLoggerMethod.Impl(Level.ERROR, this)

    override def markerState: Markers = logger.markerState
  }
}
