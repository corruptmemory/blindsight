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

import com.tersesystems.blindsight.Markers
import com.tersesystems.blindsight.mixins.MarkerMixin
import org.slf4j.Marker
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

/**
 * The logger method
 */
trait LoggerMethod {
  def level: Level

  def apply(message: String)(implicit line: Line, file: File, enclosing: Enclosing): Unit
  def apply(format: String, arg: Any)(implicit line: Line, file: File, enclosing: Enclosing): Unit
  def apply(format: String, arg1: Any, arg2: Any)(implicit line: Line,
                                                  file: File,
                                                  enclosing: Enclosing): Unit
  def apply(format: String, args: Any*)(implicit line: Line, file: File, enclosing: Enclosing): Unit
  def apply(marker: Marker,
            message: String)(implicit line: Line, file: File, enclosing: Enclosing): Unit
  def apply(marker: Marker, format: String, arg: Any)(implicit line: Line,
                                                      file: File,
                                                      enclosing: Enclosing): Unit
  def apply(marker: Marker, format: String, arg1: Any, arg2: Any)(implicit line: Line,
                                                                  file: File,
                                                                  enclosing: Enclosing): Unit
  def apply(marker: Marker, format: String, args: Any*)(implicit line: Line,
                                                        file: File,
                                                        enclosing: Enclosing): Unit
}

trait TraceLoggerMethod extends LoggerMethod {
  def level: Level = Level.TRACE
}

trait DebugLoggerMethod extends LoggerMethod {
  def level: Level = Level.DEBUG
}

trait InfoLoggerMethod extends LoggerMethod {
  def level: Level = Level.INFO
}

trait WarnLoggerMethod extends LoggerMethod {
  def level: Level = Level.WARN
}

trait ErrorLoggerMethod extends LoggerMethod {
  def level: Level = Level.ERROR
}

/**
 * This class does the work of taking various input parameters, and determining what SLF4J method to call
 * with those parameters.
 */
class SLF4JLoggerMethod(val level: Level, logger: Logger with ParameterListMixin) extends LoggerMethod {

  @inline
  protected def markerState: Markers = logger.markerState

  protected val parameterList: ParameterList = logger.parameterList(level)
  import parameterList._

  override def apply(msg: String)(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
    val markers           = collateMarkers
    if (markers.nonEmpty) {
      if (executePredicate(markers.marker)) {
        markerMessage(markers.marker, msg)
      }
    } else {
      if (executePredicate()) {
        message(msg)
      }
    }
  }

  override def apply(format: String,
                     arg: Any)(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
    val markers: Markers = collateMarkers
    if (markers.nonEmpty) {
      if (executePredicate(markers.marker)) {
        markerMessageArg1(markers.marker, format, arg)
      }
    } else {
      if (executePredicate()) {
        messageArg1(format, arg)
      }
    }
  }

  override def apply(format: String, arg1: Any, arg2: Any)(implicit line: Line,
                                                                    file: File,
                                                                    enclosing: Enclosing): Unit = {
    val markers: Markers = collateMarkers
    if (markers.nonEmpty) {
      if (executePredicate(markers.marker)) {
        markerMessageArg1Arg2(markers.marker, format, arg1, arg2)
      }
    } else {
      if (executePredicate()) {
        messageArg1Arg2(format, arg1, arg2)
      }
    }
  }

  override def apply(format: String, args: Any*)(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
    val markers           = collateMarkers
    if (markers.nonEmpty) {
      if (executePredicate(markers.marker)) {
        markerMessageArgs(markers.marker, format, args)
      }
    } else {
      if (executePredicate()) {
        messageArgs(format, args)
      }
    }
  }

  override def apply(marker: Marker, message: String)(implicit line: Line,
                                                      file: File,
                                                      enclosing: Enclosing): Unit = {
    val markers           = collateMarkers(marker)
    if (executePredicate(markers.marker)) {
      markerMessage(markers.marker, message)
    }
  }

  override def apply(marker: Marker, format: String, arg: Any)(implicit line: Line,
                                                               file: File,
                                                               enclosing: Enclosing): Unit = {
    val markers           = collateMarkers(marker)
    if (executePredicate(markers.marker)) {
      markerMessageArg1(markers.marker, format, arg)
    }
  }

  override def apply(marker: Marker, format: String, arg1: Any, arg2: Any)(
      implicit line: Line,
      file: File,
      enclosing: Enclosing): Unit = {
    val markers           = collateMarkers(marker)
    if (executePredicate(markers.marker)) {
      markerMessageArg1Arg2(markers.marker, format, arg1, arg2)
    }
  }

  override def apply(marker: Marker, format: String, args: Any*)(implicit line: Line,
                                                                 file: File,
                                                                 enclosing: Enclosing): Unit = {
    val markers           = collateMarkers(marker)
    if (executePredicate(markers.marker)) {
      markerMessageArgs(markers.marker, format, args)
    }
  }

  private def collateMarkers(implicit line: Line, file: File, enclosing: Enclosing): Markers = {
    val sourceMarker = logger.sourceInfoMarker(level, line, file, enclosing)
    sourceMarker ++ markerState
  }

  private def collateMarkers(marker: Marker)(implicit line: Line, file: File, enclosing: Enclosing): Markers = {
    collateMarkers + marker
  }

}
