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

package com.tersesystems.blindsight.api

import org.slf4j.Marker
import org.slf4j.event.Level

/**
 * This is the calling site of the SLF4J method, where parameters and arguments meet.
 */
abstract class ParameterList(val level: Level, val logger: org.slf4j.Logger) {
  def executePredicate(): Boolean
  def executePredicate(marker: Marker): Boolean

  def message: String => Unit
  def messageArg1: (String, Any) => Unit
  def messageArg1Arg2: (String, Any, Any) => Unit
  def messageArgs: (String, Seq[_]) => Unit
  def markerMessage: (Marker, String) => Unit
  def markerMessageArg1: (Marker, String, Any) => Unit
  def markerMessageArg1Arg2: (Marker, String, Any, Any) => Unit
  def markerMessageArgs: (Marker, String, Seq[_]) => Unit

  def executeStatement(statement: Statement): Unit =
    statement match {
      case Statement(markers, message, args, None) =>
        if (markers.isEmpty) {
          messageArgs(message.toString, args.asArray)
        } else {
          markerMessageArgs(markers.marker, message.toString, args.asArray)
        }

      case Statement(markers, message, args, Some(exception)) =>
        if (markers.isEmpty) {
          messageArgs(message.toString, args.asArray :+ exception)
        } else {
          markerMessageArgs(markers.marker, message.toString, args.asArray :+ exception)
        }
    }
}

object ParameterList {

  /**
   * Indexed by enum ordinal, i.e. to look up, use Level.TRACE.ordinal() as index.
   */
  def lists(logger: org.slf4j.Logger): Array[ParameterList] = Array(
    new ParameterList.Error(logger),
    new ParameterList.Warn(logger),
    new ParameterList.Info(logger),
    new ParameterList.Debug(logger),
    new ParameterList.Trace(logger)
  )

  class Trace(logger: org.slf4j.Logger) extends ParameterList(Level.TRACE, logger) {
    override def executePredicate(): Boolean = {
      logger.isTraceEnabled()
    }
    override def executePredicate(marker: Marker): Boolean = {
      logger.isTraceEnabled(marker)
    }

    val message: String => Unit                                   = logger.trace
    val messageArg1: (String, Any) => Unit                        = logger.trace
    val messageArg1Arg2: (String, Any, Any) => Unit               = logger.trace
    val messageArgs: (String, Seq[_]) => Unit                     = logger.trace(_, _: _*)
    val markerMessage: (Marker, String) => Unit                   = logger.trace
    val markerMessageArg1: (Marker, String, Any) => Unit          = logger.trace
    val markerMessageArg1Arg2: (Marker, String, Any, Any) => Unit = logger.trace
    val markerMessageArgs: (Marker, String, Seq[_]) => Unit       = logger.trace(_, _, _: _*)
  }

  class Debug(logger: org.slf4j.Logger) extends ParameterList(Level.DEBUG, logger) {
    override def executePredicate(): Boolean               = logger.isDebugEnabled()
    override def executePredicate(marker: Marker): Boolean = logger.isDebugEnabled(marker)

    val message: String => Unit                                   = logger.debug
    val messageArg1: (String, Any) => Unit                        = logger.debug
    val messageArg1Arg2: (String, Any, Any) => Unit               = logger.debug
    val messageArgs: (String, Seq[_]) => Unit                     = logger.debug(_, _: _*)
    val markerMessage: (Marker, String) => Unit                   = logger.debug
    val markerMessageArg1: (Marker, String, Any) => Unit          = logger.debug
    val markerMessageArg1Arg2: (Marker, String, Any, Any) => Unit = logger.debug
    val markerMessageArgs: (Marker, String, Seq[_]) => Unit       = logger.debug(_, _, _: _*)
  }

  class Info(logger: org.slf4j.Logger) extends ParameterList(Level.INFO, logger) {
    override def executePredicate(): Boolean               = logger.isInfoEnabled
    override def executePredicate(marker: Marker): Boolean = logger.isInfoEnabled(marker)

    val message: String => Unit                                   = logger.info
    val messageArg1: (String, Any) => Unit                        = logger.info
    val messageArg1Arg2: (String, Any, Any) => Unit               = logger.info
    val messageArgs: (String, Seq[_]) => Unit                     = logger.info(_, _: _*)
    val markerMessage: (Marker, String) => Unit                   = logger.info
    val markerMessageArg1: (Marker, String, Any) => Unit          = logger.info
    val markerMessageArg1Arg2: (Marker, String, Any, Any) => Unit = logger.info
    val markerMessageArgs: (Marker, String, Seq[_]) => Unit       = logger.info(_, _, _: _*)

  }

  class Warn(logger: org.slf4j.Logger) extends ParameterList(Level.WARN, logger) {
    override def executePredicate(): Boolean               = logger.isWarnEnabled()
    override def executePredicate(marker: Marker): Boolean = logger.isWarnEnabled(marker)

    val message: String => Unit                                   = logger.warn
    val messageArg1: (String, Any) => Unit                        = logger.warn
    val messageArg1Arg2: (String, Any, Any) => Unit               = logger.warn
    val messageArgs: (String, Seq[_]) => Unit                     = logger.warn(_, _: _*)
    val markerMessage: (Marker, String) => Unit                   = logger.warn
    val markerMessageArg1: (Marker, String, Any) => Unit          = logger.warn
    val markerMessageArg1Arg2: (Marker, String, Any, Any) => Unit = logger.warn
    val markerMessageArgs: (Marker, String, Seq[_]) => Unit       = logger.warn(_, _, _: _*)
  }

  class Error(logger: org.slf4j.Logger) extends ParameterList(Level.ERROR, logger) {
    override def executePredicate(): Boolean               = logger.isErrorEnabled
    override def executePredicate(marker: Marker): Boolean = logger.isErrorEnabled(marker)

    val message: String => Unit                                   = logger.error
    val messageArg1: (String, Any) => Unit                        = logger.error
    val messageArg1Arg2: (String, Any, Any) => Unit               = logger.error
    val messageArgs: (String, Seq[_]) => Unit                     = logger.error(_, _: _*)
    val markerMessage: (Marker, String) => Unit                   = logger.error
    val markerMessageArg1: (Marker, String, Any) => Unit          = logger.error
    val markerMessageArg1Arg2: (Marker, String, Any, Any) => Unit = logger.error
    val markerMessageArgs: (Marker, String, Seq[_]) => Unit       = logger.error(_, _, _: _*)
  }
}
