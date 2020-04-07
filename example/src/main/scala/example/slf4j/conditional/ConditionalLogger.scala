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

package example.slf4j.conditional

import com.tersesystems.blindsight.{Arguments, Statement, ToMessage}
import com.tersesystems.blindsight.slf4j._
import org.slf4j.Marker
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

class ConditionalLogger(test: => Boolean, protected val logger: Logger with ParameterListMixin)
    extends LoggerAPI[LoggerPredicate, ConditionalLoggerMethod] {
  override type Self      = ConditionalLogger
  override type Method    = ConditionalLoggerMethod
  override type Predicate = logger.Predicate

  def onCondition(test2: => Boolean): Self = new ConditionalLogger(test && test2, logger)

  override def isTraceEnabled: Predicate = logger.isTraceEnabled
  override def trace: Method             = new ConditionalLoggerMethod(Level.TRACE, test, logger)

  override def isDebugEnabled: Predicate = logger.isDebugEnabled
  override def debug: Method             = new ConditionalLoggerMethod(Level.DEBUG, test, logger)

  override def isInfoEnabled: Predicate = logger.isInfoEnabled
  override def info: Method             = new ConditionalLoggerMethod(Level.INFO, test, logger)

  override def isWarnEnabled: Predicate = logger.isWarnEnabled
  override def warn: Method             = new ConditionalLoggerMethod(Level.WARN, test, logger)

  override def isErrorEnabled: Predicate = logger.isErrorEnabled
  override def error: Method             = new ConditionalLoggerMethod(Level.ERROR, test, logger)
}

class ConditionalLoggerMethod(
    val level: Level,
    test: => Boolean,
    logger: Logger with ParameterListMixin
) extends LoggerMethod {

  protected val parameterList: ParameterList = logger.parameterList(level)

  override def apply[M: ToMessage](
      instance: => M)(implicit line: Line, file: File, enclosing: Enclosing): Unit = if (test) {
    parameterList.message(implicitly[ToMessage[M]].toMessage(instance).toString)
  }

  override def apply[M: ToMessage](instance: => M, args: Arguments)(implicit line: Line,
                                                             file: File,
                                                             enclosing: Enclosing): Unit = if (test) {
    parameterList.messageArgs(implicitly[ToMessage[M]].toMessage(instance).toString, args.asArray)
  }

  override def apply[M: ToMessage](instance: => M, arg: Any)(implicit line: Line,
                                                             file: File,
                                                             enclosing: Enclosing): Unit = if (test) {
    parameterList.messageArg1(implicitly[ToMessage[M]].toMessage(instance).toString, arg)
  }

  override def apply[M: ToMessage](instance: => M, arg1: Any, arg2: Any)(
      implicit line: Line,
      file: File,
      enclosing: Enclosing): Unit = if (test) {
    parameterList.messageArg1Arg2(implicitly[ToMessage[M]].toMessage(instance).toString, arg1, arg2)
  }

  override def apply[M: ToMessage](instance: => M, args: Any*)(implicit line: Line,
                                                               file: File,
                                                               enclosing: Enclosing): Unit = if (test) {
    parameterList.messageArgs(implicitly[ToMessage[M]].toMessage(instance).toString, args)
  }

  override def apply[M: ToMessage](
      marker: => Marker,
      instance: => M)(implicit line: Line, file: File, enclosing: Enclosing): Unit = if (test) {
    parameterList.markerMessage(marker, implicitly[ToMessage[M]].toMessage(instance).toString)
  }

  override def apply[M: ToMessage](marker: => Marker, instance: => M, args: Arguments)(
    implicit line: Line,
    file: File,
    enclosing: Enclosing): Unit = if (test) {
    parameterList.markerMessageArgs(marker, implicitly[ToMessage[M]].toMessage(instance).toString, args.asArray)
  }

  override def apply[M: ToMessage](marker: => Marker, instance: => M, arg: Any)(
      implicit line: Line,
      file: File,
      enclosing: Enclosing): Unit = if (test) {
    parameterList.markerMessageArg1(marker, implicitly[ToMessage[M]].toMessage(instance).toString, arg)
  }

  override def apply[M: ToMessage](marker: => Marker, instance: => M, arg1: Any, arg2: Any)(
      implicit line: Line,
      file: File,
      enclosing: Enclosing): Unit = if (test) {
    parameterList.markerMessageArg1Arg2(marker, implicitly[ToMessage[M]].toMessage(instance).toString, arg1, arg2)
  }

  override def apply[M: ToMessage](marker: => Marker, instance: => M, args: Any*)(
      implicit line: Line,
      file: File,
      enclosing: Enclosing): Unit = if (test) {
    parameterList.markerMessageArgs(marker, implicitly[ToMessage[M]].toMessage(instance).toString, args)
  }

}
