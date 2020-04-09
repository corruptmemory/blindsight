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

package com.tersesystems.blindsight.logback

import com.tersesystems.blindsight.api._
import com.tersesystems.blindsight.fluent.FluentLogger
import com.tersesystems.blindsight.logstash.LogstashSourceInfoMixin
import com.tersesystems.blindsight.semantic.SemanticLogger
import com.tersesystems.blindsight.slf4j.SLF4JLogger.Conditional
import com.tersesystems.blindsight.slf4j._
import com.tersesystems.blindsight.{Logger, LoggerFactory}
import org.slf4j
import org.slf4j.event.Level

class LogbackLoggerFactory extends LoggerFactory {
  override def getLogger[T: LoggerResolver](instance: T): Logger = {
    val underlying = implicitly[LoggerResolver[T]].resolveLogger(instance)
    new LogbackLogger(new LogbackSLF4JLogger(underlying, Markers.empty))
  }

  class LogbackLogger(protected val logger: ExtendedSLF4JLogger)
      extends Logger
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
      new LogbackLogger(logger.onCondition(test).asInstanceOf[ExtendedSLF4JLogger])
    }

    override def withMarker[T: ToMarkers](markerInstance: T): Self =
      new LogbackLogger(logger.withMarker(markerInstance).asInstanceOf[ExtendedSLF4JLogger])

    override def markers: Markers = logger.markers

    override def underlying: org.slf4j.Logger = logger.underlying
  }

  class LogbackSLF4JLogger(underlying: org.slf4j.Logger, markers: Markers)
      extends SLF4JLogger.Impl(underlying, markers)
      with LogstashSourceInfoMixin {

    override def onCondition(test: => Boolean): Self = {
      new Conditional(test, this)
    }

    override def withMarker[T: ToMarkers](markerInst: T): Self = {
      val markers = implicitly[ToMarkers[T]].toMarkers(markerInst)
      self(underlying, markers ++ markers)
    }

    override protected def self(underlying: slf4j.Logger, markerState: Markers): SLF4JLogger = {
      new LogbackSLF4JLogger(underlying, markerState)
    }
  }
}
