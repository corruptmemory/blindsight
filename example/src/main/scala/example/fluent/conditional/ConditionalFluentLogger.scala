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

package example.fluent.conditional

import com.tersesystems.blindsight.ToStatement
import com.tersesystems.blindsight.fluent.{SLF4JFluentLogger, SLF4JFluentLoggerMethod}
import com.tersesystems.blindsight.slf4j.{LoggerAPI, LoggerPredicate}
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

class ConditionalFluentLogger(test: => Boolean, logger: SLF4JFluentLogger)
    extends LoggerAPI[LoggerPredicate, ConditionalFluentLoggerMethod] {
  override type Predicate = LoggerPredicate
  override type Method    = ConditionalFluentLoggerMethod
  override type Self      = ConditionalFluentLogger

  def onCondition(test2: Boolean): Self = new ConditionalFluentLogger(test && test2, logger)

  override def isTraceEnabled: Predicate = logger.isTraceEnabled
  override def trace: Method             = new ConditionalFluentLoggerMethod(Level.TRACE, test, logger)

  override def isDebugEnabled: Predicate = logger.isDebugEnabled
  override def debug: Method             = new ConditionalFluentLoggerMethod(Level.DEBUG, test, logger)

  override def isInfoEnabled: Predicate = logger.isInfoEnabled
  override def info: Method             = new ConditionalFluentLoggerMethod(Level.INFO, test, logger)

  override def isWarnEnabled: Predicate = logger.isWarnEnabled
  override def warn: Method             = new ConditionalFluentLoggerMethod(Level.WARN, test, logger)

  override def isErrorEnabled: Predicate = logger.isErrorEnabled
  override def error: Method             = new ConditionalFluentLoggerMethod(Level.ERROR, test, logger)
}

class ConditionalFluentLoggerMethod(level: Level, test: => Boolean, logger: SLF4JFluentLogger)
    extends SLF4JFluentLoggerMethod(level, logger) {

  override def apply[T: ToStatement](
      instance: => T
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
    if (test) {
      val statement = implicitly[ToStatement[T]].toStatement(instance)
      logger.parameterList(level).executeStatement(statement)
    }
  }
}
