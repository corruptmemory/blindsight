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

package com.tersesystems.blindsight.logstash

import com.tersesystems.blindsight.api._
import com.tersesystems.blindsight.slf4j._
import com.tersesystems.blindsight.{Logger, LoggerFactory}

class LogstashLoggerFactory extends LoggerFactory {
  override def getLogger[T: LoggerResolver](instance: T): Logger = {
    val underlying = implicitly[LoggerResolver[T]].resolveLogger(instance)
    new Logger.Impl(new LogstashSLF4JLogger(underlying, Markers.empty))
  }

  /**
   * Extends the logback logger with logstash markers on source info.
   *
   * @param underlying the slf4j logger.
   * @param markers    the marker state on the logger.
   */
  class LogstashSLF4JLogger(underlying: org.slf4j.Logger, markers: Markers)
    extends Logger.SLF4J(underlying, markers) with LogstashSourceInfoMixin {

    override protected def self(underlying: org.slf4j.Logger, markerState: Markers): SLF4JLogger = {
      new LogstashSLF4JLogger(underlying, markerState)
    }
  }

}
