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

package com.tersesystems.blindsight

import com.tersesystems.blindsight.api._
import com.tersesystems.blindsight.fluent.FluentLogger
import com.tersesystems.blindsight.semantic.SemanticLogger
import com.tersesystems.blindsight.slf4j.{SLF4JLogger, _}
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

trait Logger extends SLF4JLogger {
  def fluent: FluentLogger

  def refine[MessageType]: SemanticLogger[MessageType]
}

object Logger {

  class Impl(protected val logger: ExtendedSLF4JLogger)
    extends Logger
      with SLF4JLoggerAPI.Proxy[SLF4JLoggerPredicate, SLF4JLoggerMethod] {
    override type Parent = SLF4JLogger
    override type Self = Logger

    override def fluent: FluentLogger = {
      new FluentLogger.Impl(logger)
    }

    override def refine[MessageType]: SemanticLogger[MessageType] = {
      new SemanticLogger.Impl[MessageType](logger)
    }

    override def onCondition(test: => Boolean): Self = {
      new Impl(logger.onCondition(test).asInstanceOf[ExtendedSLF4JLogger])
    }

    override def withMarker[T: ToMarkers](markerInstance: T): Self =
      new Impl(logger.withMarker(markerInstance).asInstanceOf[ExtendedSLF4JLogger])

    override def markers: Markers = logger.markers

    override def underlying: org.slf4j.Logger = logger.underlying
  }

  class SLF4J(underlying: org.slf4j.Logger, markers: Markers)
    extends SLF4JLogger.Impl(underlying, markers) {

    override def onCondition(test: => Boolean): Self = {
      new SLF4JLogger.Conditional(test, this)
    }

    override def withMarker[T: ToMarkers](markerInst: T): Self = {
      val markers = implicitly[ToMarkers[T]].toMarkers(markerInst)
      self(underlying, markers ++ markers)
    }

    override protected def self(underlying: org.slf4j.Logger, markerState: Markers): SLF4JLogger = {
      new SLF4J(underlying, markerState)
    }

    override def sourceInfoMarker(level: Level, line: Line, file: File, enclosing: Enclosing): Markers = Markers.empty
  }

}