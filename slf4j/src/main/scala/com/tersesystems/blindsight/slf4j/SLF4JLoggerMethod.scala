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

package com.tersesystems.blindsight.slf4j

import com.tersesystems.blindsight.api._
import org.slf4j.Marker
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

/**
 * The logger method
 */
trait SLF4JLoggerMethod {
  def level: Level

  def when(condition: => Boolean)(block: SLF4JLoggerMethod => Unit): Unit

  def apply[M: ToMessage](
                           instance: M
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[M: ToMessage](
                           instance: M,
                           arg: Any
                         )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[M: ToMessage](
                           format: M,
                           args: Arguments
                         )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[M: ToMessage](instance: M, arg1: Any, arg2: Any)(
    implicit line: Line,
    file: File,
    enclosing: Enclosing
  ): Unit

  def apply[M: ToMessage](
                           instance: M,
                           args: Any*
                         )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[M: ToMessage](
                           markers: Marker,
                           message: M
                         )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[M: ToMessage](markers: Marker, messageInstance: M, args: Arguments)(
    implicit line: Line,
    file: File,
    enclosing: Enclosing
  ): Unit

  def apply[M: ToMessage](markers: Marker, messageInstance: M, arg: Any)(
    implicit line: Line,
    file: File,
    enclosing: Enclosing
  ): Unit

  def apply[M: ToMessage](markers: Marker, messageInstance: M, arg1: Any, arg2: Any)(
    implicit line: Line,
    file: File,
    enclosing: Enclosing
  ): Unit

  def apply[M: ToMessage](markers: Marker, messageInstance: M, args: Any*)(
    implicit line: Line,
    file: File,
    enclosing: Enclosing
  ): Unit
}

object SLF4JLoggerMethod {

  /**
   * This class does the work of taking various input parameters, and determining what SLF4J method to call
   * with those parameters.
   */
  class Impl(val level: Level, logger: ExtendedSLF4JLogger) extends SLF4JLoggerMethod {

    @inline
    protected def markers: Markers = logger.markers

    protected val parameterList: ParameterList = logger.parameterList(level)

    import parameterList._

    override def apply[M: ToMessage](
                                      instance: M
                                    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val markers = collateMarkers
      if (markers.nonEmpty) {
        if (executePredicate(markers.marker)) {
          val message1 = implicitly[ToMessage[M]].toMessage(instance)
          markerMessage(markers.marker, message1.toString)
        }
      } else {
        if (executePredicate()) {
          val message1 = implicitly[ToMessage[M]].toMessage(instance)
          message(message1.toString)
        }
      }
    }

    override def apply[M: ToMessage](
                                      format: M,
                                      arg: Any
                                    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val markers: Markers = collateMarkers
      if (markers.nonEmpty) {
        if (executePredicate(markers.marker)) {
          val message1 = implicitly[ToMessage[M]].toMessage(format)
          markerMessageArg1(markers.marker, message1.toString, arg)
        }
      } else {
        if (executePredicate()) {
          val message1 = implicitly[ToMessage[M]].toMessage(format)
          messageArg1(message1.toString, arg)
        }
      }
    }

    override def apply[M: ToMessage](
                                      format: M,
                                      args: Arguments
                                    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val markers: Markers = collateMarkers
      if (markers.nonEmpty) {
        if (executePredicate(markers.marker)) {
          val message1 = implicitly[ToMessage[M]].toMessage(format)
          markerMessageArgs(markers.marker, message1.toString, args.asArray)
        }
      } else {
        if (executePredicate()) {
          val message1 = implicitly[ToMessage[M]].toMessage(format)
          messageArgs(message1.toString, args.asArray)
        }
      }
    }

    override def apply[M: ToMessage](
                                      format: M,
                                      arg1: Any,
                                      arg2: Any
                                    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val markers: Markers = collateMarkers
      if (markers.nonEmpty) {
        if (executePredicate(markers.marker)) {
          val message1 = implicitly[ToMessage[M]].toMessage(format)
          markerMessageArg1Arg2(markers.marker, message1.toString, arg1, arg2)
        }
      } else {
        if (executePredicate()) {
          val message1 = implicitly[ToMessage[M]].toMessage(format)
          messageArg1Arg2(message1.toString, arg1, arg2)
        }
      }
    }

    override def apply[M: ToMessage](
                                      format: M,
                                      args: Any*
                                    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val markers = collateMarkers
      if (markers.nonEmpty) {
        if (executePredicate(markers.marker)) {
          val message1 = implicitly[ToMessage[M]].toMessage(format)
          markerMessageArgs(markers.marker, message1.toString, args)
        }
      } else {
        if (executePredicate()) {
          val message1 = implicitly[ToMessage[M]].toMessage(format)
          messageArgs(message1.toString, args)
        }
      }
    }

    override def apply[M: ToMessage](
                                      marker: Marker,
                                      messageInstance: M
                                    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val markers = collateMarkers(marker)
      if (executePredicate(markers.marker)) {
        val message1 = implicitly[ToMessage[M]].toMessage(messageInstance)
        markerMessage(markers.marker, message1.toString())
      }
    }

    override def apply[M: ToMessage](marker: Marker, instance: M, args: Arguments)(
      implicit line: Line,
      file: File,
      enclosing: Enclosing
    ): Unit = {
      val markers = collateMarkers(marker)
      if (executePredicate(markers.marker)) {
        val message1 = implicitly[ToMessage[M]].toMessage(instance)
        markerMessageArgs(markers.marker, message1.toString, args.asArray)
      }
    }

    override def apply[M: ToMessage](
                                      marker: Marker,
                                      format: M,
                                      arg: Any
                                    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val markers = collateMarkers(marker)
      if (executePredicate(markers.marker)) {
        val message1 = implicitly[ToMessage[M]].toMessage(format)
        markerMessageArg1(markers.marker, message1.toString, arg)
      }
    }

    override def apply[M: ToMessage](marker: Marker, format: M, arg1: Any, arg2: Any)(
      implicit line: Line,
      file: File,
      enclosing: Enclosing
    ): Unit = {
      val markers = collateMarkers(marker)
      if (executePredicate(markers.marker)) {
        val message1 = implicitly[ToMessage[M]].toMessage(format)
        markerMessageArg1Arg2(markers.marker, message1.toString, arg1, arg2)
      }
    }

    override def apply[M: ToMessage](
                                      marker: Marker,
                                      format: M,
                                      args: Any*
                                    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val markers = collateMarkers(marker)
      if (executePredicate(markers.marker)) {
        val message1 = implicitly[ToMessage[M]].toMessage(format)
        markerMessageArgs(markers.marker, message1.toString, args)
      }
    }

    private def collateMarkers(implicit line: Line, file: File, enclosing: Enclosing): Markers = {
      val sourceMarker: Markers = logger.sourceInfoMarker(level, line, file, enclosing)
      sourceMarker ++ markers
    }

    private def collateMarkers[MR: ToMarkers](
                                               marker: MR
                                             )(implicit line: Line, file: File, enclosing: Enclosing): Markers = {
      collateMarkers ++ implicitly[ToMarkers[MR]].toMarkers(marker)
    }

    override def when(condition: => Boolean)(block: SLF4JLoggerMethod => Unit): Unit = {
      if (condition && executePredicate(collateMarkers.marker)) {
        block(this)
      }
    }
  }

  class Conditional(
      val level: Level,
      test: => Boolean,
      logger: ExtendedSLF4JLogger
  ) extends SLF4JLoggerMethod {

    private def method = logger.method(level)

    override def apply[M: ToMessage](
                                      instance: M
                                    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = if (test) {
      method(instance)
    }

    override def apply[M: ToMessage](
                                      instance: M,
                                      args: Arguments
                                    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = if (test) {
      method(instance, args)
    }

    override def apply[M: ToMessage](
                                      instance: M,
                                      arg: Any
                                    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = if (test) {
      method.apply[M](instance: M, arg: Any)
    }

    override def apply[M: ToMessage](
                                      instance: M,
                                      arg1: Any,
                                      arg2: Any
                                    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = if (test) {
      method.apply[M](instance: M, arg1: Any, arg2)
    }

    override def apply[M: ToMessage](
                                      instance: M,
                                      args: Any*
                                    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = if (test) {
      method.apply[M](instance: M, args: _*)
    }

    override def apply[M: ToMessage](
                                      marker: Marker,
                                      instance: M
                                    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = if (test) {
      method.apply[M](marker, instance: M)
    }

    override def apply[M: ToMessage](
                                      marker: Marker,
                                      instance: M,
                                      args: Arguments
                                    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = if (test) {
      method.apply[M](marker, instance: M, args)
    }

    override def apply[M: ToMessage](
                                      marker: Marker,
                                      instance: M,
                                      arg: Any
                                    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = if (test) {
      method.apply[M](marker, instance: M, arg: Any)
    }

    override def apply[M: ToMessage](marker: Marker, instance: M, arg1: Any, arg2: Any)(
      implicit line: Line,
      file: File,
      enclosing: Enclosing
    ): Unit = if (test) {
      method.apply[M](marker, instance: M, arg1, arg2)
    }

    override def apply[M: ToMessage](
                                      marker: Marker,
                                      instance: M,
                                      args: Any*
                                    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = if (test) {
      method.apply[M](marker, instance: M, args: _*)
    }

    override def when(condition: => Boolean)(block: SLF4JLoggerMethod => Unit): Unit = {
      if (test && condition) {
        block(this)
      }
    }
  }
}
