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

import com.tersesystems.blindsight._
import com.tersesystems.blindsight.mixins.{SemanticMarkerMixin, SourceInfoMixin}
import com.tersesystems.blindsight.slf4j.{LoggerPredicate, ParameterList, SLF4JLogger}
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

/**
 * This trait defines an SLF4J compatible logger with all five levels of logging.
 */
trait SemanticLogger[MessageType]
    extends SemanticLoggerAPI[MessageType, LoggerPredicate, SemanticLoggerMethod]
    with SemanticMarkerMixin[MessageType]
    with SemanticRefineMixin[MessageType]
    with SourceInfoMixin {
  type Self[T] = SemanticLogger[T]
}

object SemanticLogger {
  def apply[MessageType](logger: org.slf4j.Logger): SemanticLogger[MessageType] = {
    SLF4JSemanticLogger(logger)
  }
}

class SLF4JSemanticLogger[BaseType](protected val logger: SLF4JLogger, val markerState: Markers)
    extends SemanticLogger[BaseType]
    with SourceInfoMixin {

  private val methods: Array[Method[BaseType]] = Array(
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

  def method(level: Level): Method[BaseType] = methods(level.ordinal())

  def predicate(level: Level): Predicate = predicates(level.ordinal())

  def parameterList(level: Level): ParameterList = logger.parameterList(level)

  override def marker[T: ToMarkers](markerInst: T): Self[BaseType] = {
    val markers = implicitly[ToMarkers[T]].toMarkers(markerInst)
    new SLF4JSemanticLogger[BaseType](logger, markerState ++ markers)
  }

  override def refine[T <: BaseType: ToStatement]: Self[T] =
    new SLF4JSemanticLogger[T](logger, markerState)

  override def sourceInfoMarker(
      level: Level,
      line: Line,
      file: File,
      enclosing: Enclosing
  ): Markers = Markers.empty

  override def isTraceEnabled: Predicate = logger.predicate(Level.TRACE)
  override def trace: Method[BaseType]   = new SLF4JSemanticLoggerMethod(Level.TRACE, this)

  override def isDebugEnabled: Predicate = logger.predicate(Level.DEBUG)
  override def debug: Method[BaseType]   = new SLF4JSemanticLoggerMethod(Level.DEBUG, this)

  override def isInfoEnabled: Predicate = logger.predicate(Level.INFO)
  override def info: Method[BaseType]   = new SLF4JSemanticLoggerMethod(Level.INFO, this)

  override def isWarnEnabled: Predicate = logger.predicate(Level.WARN)
  override def warn: Method[BaseType]   = new SLF4JSemanticLoggerMethod(Level.WARN, this)

  override def isErrorEnabled: Predicate = logger.predicate(Level.ERROR)
  override def error: Method[BaseType]   = new SLF4JSemanticLoggerMethod(Level.ERROR, this)

}

object SLF4JSemanticLogger {
  def apply[BaseType](logger: SLF4JLogger): SLF4JSemanticLogger[BaseType] = {
    new SLF4JSemanticLogger[BaseType](logger, Markers.empty)
  }

  def apply[BaseType](logger: org.slf4j.Logger): SLF4JSemanticLogger[BaseType] = {
    apply(new SLF4JLogger(logger, Markers.empty))
  }
}
