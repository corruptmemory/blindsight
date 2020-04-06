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

import com.tersesystems.blindsight._
import com.tersesystems.blindsight.mixins.{MarkerMixin, SourceInfoMixin}
import com.tersesystems.blindsight.slf4j._
import org.slf4j
import org.slf4j.event.Level
import org.slf4j.event.Level._
import sourcecode.{Enclosing, File, Line}

trait FluentLogger
    extends LoggerAPI[LoggerPredicate, FluentLoggerMethod]
    with MarkerMixin
    with SourceInfoMixin

object FluentLogger {
  def apply(underlying: slf4j.Logger): FluentLogger = {
    new SLF4JFluentLogger(new SLF4JLogger(underlying, Markers.empty))
  }
}

class SLF4JFluentLogger(logger: SLF4JLogger) extends FluentLogger with ParameterListMixin {
  override type Self      = SLF4JFluentLogger
  override type Method    = SLF4JFluentLoggerMethod
  override type Predicate = LoggerPredicate

  override def isTraceEnabled: Predicate = logger.predicate(TRACE)
  override def trace: Method             = new SLF4JFluentLoggerMethod(TRACE, this)

  override def isDebugEnabled: Predicate = logger.predicate(DEBUG)
  override def debug: Method             = new SLF4JFluentLoggerMethod(DEBUG, this)

  override def isInfoEnabled: Predicate = logger.predicate(INFO)
  override def info: Method             = new SLF4JFluentLoggerMethod(INFO, this)

  override def isWarnEnabled: Predicate = logger.predicate(WARN)
  override def warn: Method             = new SLF4JFluentLoggerMethod(WARN, this)

  override def isErrorEnabled: Predicate = logger.predicate(ERROR)
  override def error: Method             = new SLF4JFluentLoggerMethod(ERROR, this)

  def parameterList(level: Level): ParameterList = logger.parameterList(level)
  def markerState: Markers                       = logger.markerState

  override def marker[T: ToMarkers](markerInstance: T): Self = {
    new SLF4JFluentLogger(logger.marker(markerInstance))
  }

  override def sourceInfoMarker(
      level: Level,
      line: Line,
      file: File,
      enclosing: Enclosing
  ): Markers = Markers.empty
}
