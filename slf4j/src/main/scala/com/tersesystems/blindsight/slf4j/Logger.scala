/*
 * Copyright 2020 Will Sargent
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

import com.tersesystems.blindsight._
import com.tersesystems.blindsight.mixins.{MarkerMixin, SourceInfoMixin}
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

trait Logger extends LoggerAPI[LoggerPredicate, LoggerMethod] with MarkerMixin with SourceInfoMixin {
  override type Self <: Logger
}

object Logger {
  def apply(underlying: org.slf4j.Logger): Logger = {
    new SLF4JLogger(underlying, Markers.empty)
  }
}

class SLF4JLogger(val underlying: org.slf4j.Logger, val markerState: Markers) extends Logger with ParameterListMixin {
  override type Self = SLF4JLogger
  override type Method = SLF4JLoggerMethod
  override type Predicate = LoggerPredicate

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

  def predicate(level: Level): Predicate = predicates(level.ordinal())

  @inline
  def parameterList(level: Level): ParameterList = parameterLists(level.ordinal)

  override def sourceInfoMarker(level: Level, line: Line, file: File, enclosing: Enclosing): Markers =
    Markers.empty

  override def isTraceEnabled: Predicate = new SLF4JLoggerPredicate(Level.TRACE, this)
  override def trace: Method             = new SLF4JLoggerMethod(Level.TRACE, this)

  override def isDebugEnabled: Predicate = new SLF4JLoggerPredicate(Level.DEBUG, this)
  override def debug: Method             = new SLF4JLoggerMethod(Level.DEBUG, this)

  override def isInfoEnabled: Predicate = new SLF4JLoggerPredicate(Level.INFO, this)
  override def info: Method             = new SLF4JLoggerMethod(Level.INFO, this)

  override def isWarnEnabled: Predicate = new SLF4JLoggerPredicate(Level.WARN, this)
  override def warn: Method             = new SLF4JLoggerMethod(Level.WARN, this)

  override def isErrorEnabled: Predicate = new SLF4JLoggerPredicate(Level.ERROR, this)
  override def error: Method             = new SLF4JLoggerMethod(Level.ERROR, this)

  override def marker[T: ToMarkers](markerInst: T): Self = {
    val markers = implicitly[ToMarkers[T]].toMarkers(markerInst)
    new SLF4JLogger(underlying, markerState ++ markers)
  }
}
