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

package example.flow

import com.tersesystems.blindsight.api.{Markers, Statement, ToStatement}
import com.tersesystems.blindsight.slf4j.SLF4JLogger
import com.tersesystems.blindsight.slf4j.SLF4JLogger.Impl
import org.slf4j.Logger
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

object FlowMain {

  protected class NoSourceSLF4JLogger(
      underlying: org.slf4j.Logger,
      markers: Markers = Markers.empty
  ) extends Impl(underlying, markers) {
    override protected def self(underlying: Logger, markerState: Markers): SLF4JLogger = {
      new NoSourceSLF4JLogger(underlying, markerState)
    }

    override def sourceInfoMarker(
        level: Level,
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Markers = Markers.empty
  }

  def main(args: Array[String]): Unit = {
    val underlying = org.slf4j.LoggerFactory.getLogger(getClass)
    val logger     = new FlowLogger(new NoSourceSLF4JLogger(underlying))

    logger.info("About to execute flow")

    // Need to specify to statement type class for the exit type:
    implicit val toIntStatement: ToStatement[Int] =
      ToStatement(i => Statement().withMessage(i.toString))
    val result: Int = logger.entry("entering") { exit => exit(1 + 2) }
  }
}
