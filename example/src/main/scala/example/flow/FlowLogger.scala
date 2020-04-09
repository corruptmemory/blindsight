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

import com.tersesystems.blindsight.api.{Markers, ParameterList, ToMarkers, ToStatement}
import com.tersesystems.blindsight.slf4j._
import org.slf4j.event.Level
import org.slf4j.{Logger, MarkerFactory}
import sourcecode.{Enclosing, File, Line}

class FlowLogger(protected val logger: ExtendedSLF4JLogger)
    extends SLF4JLoggerAPI.Proxy[SLF4JLoggerPredicate, SLF4JLoggerMethod]
    with ExtendedSLF4JLogger {
  override type Parent = SLF4JLogger
  override type Self   = FlowLogger

  private val entryLogger = logger.withMarker(MarkerFactory.getMarker("ENTRY"))
  private val exitLogger  = logger.withMarker(MarkerFactory.getMarker("EXIT"))

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

  override def withMarker[T: ToMarkers](markerInstance: T) =
    new FlowLogger(logger.withMarker(markerInstance).asInstanceOf[ExtendedSLF4JLogger])

  override def markers: Markers = logger.markers

  override def parameterList(level: Level): ParameterList = logger.parameterList(level)

  override def method(level: Level): SLF4JLoggerMethod = logger.method(level)

  override def predicate(level: Level): SLF4JLoggerPredicate = logger.predicate(level)

  override def underlying: Logger = logger.underlying

  override def onCondition(test: => Boolean): FlowLogger = new FlowLogger(logger)
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
