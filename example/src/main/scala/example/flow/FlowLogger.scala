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

import com.tersesystems.blindsight._
import com.tersesystems.blindsight.slf4j._
import org.slf4j.MarkerFactory
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

class FlowLogger(protected val logger: Logger)
    extends LoggerAPI.Proxy[LoggerPredicate, LoggerMethod]
    with Logger {
  override type Parent = Logger
  override type Self   = FlowLogger

  private val entryLogger = logger.marker(MarkerFactory.getMarker("ENTRY"))
  private val exitLogger  = logger.marker(MarkerFactory.getMarker("EXIT"))

  def entry: LoggerFlowMethod = {
    new LoggerFlowMethod {
      override def apply[B](
          message: String
      )(block: ExitHandle => B)(implicit enclosing: Enclosing): B = {
        entryLogger.trace(s"${enclosing.value} enter: $message")
        block(new ExitHandle {
          override def apply[T: ToStatement](instance: => T): T = {
            val result = instance
            val s      = implicitly[ToStatement[T]].toStatement(result)
            exitLogger.trace(
              s"${enclosing.value} exit: result = ${s.message.toString}",
              s.arguments.asArray: _*
            )
            result
          }
        })
      }
    }
  }

  override def sourceInfoMarker(
      level: Level,
      line: Line,
      file: File,
      enclosing: Enclosing
  ): Markers = Markers.empty

  override def marker[T: ToMarkers](markerInstance: T) =
    new FlowLogger(logger.marker(markerInstance))

  override def markerState: Markers = logger.markerState
}

/**
 * This trait is an extension on the logger method that allows for
 * flow control using entry / exit handles with logging.
 */
trait LoggerFlowMethod {

  trait ExitHandle {
    def apply[T: ToStatement](instance: => T): T
  }

  def apply[B](message: String)(block: ExitHandle => B)(implicit enclosing: Enclosing): B
}
