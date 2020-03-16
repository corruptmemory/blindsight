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

package com.tersesystems.blindsight.fluent

import com.tersesystems.blindsight._
import com.tersesystems.blindsight.slf4j.ParameterList
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

trait FluentLoggerMethod extends FluentAPI {
  def apply[T: ToStatement](instance: => T)(implicit line: Line, file: File, enclosing: Enclosing): Unit
}

object FluentLoggerMethod {
  trait Builder extends FluentAPI {
    def log(): Unit
    def logWithPlaceholders(): Unit
  }
}

class SLF4JFluentLoggerMethod(val level: Level, logger: SLF4JFluentLogger) extends FluentLoggerMethod {

  protected val parameterList: ParameterList = logger.parameterList(level)
  protected[fluent] def markerState: Markers = logger.markerState

  final case class BuilderImpl(
      mkrs: Markers,
      m: Message,
      args: Arguments,
      e: Option[Throwable]
  ) extends FluentLoggerMethod.Builder {

    override def marker[T: ToMarkers](instance: => T): FluentLoggerMethod.Builder = {
      val moreMarkers = implicitly[ToMarkers[T]].toMarkers(instance)
      copy(mkrs = mkrs ++ moreMarkers)
    }

    override def message[T: ToMessage](instance: => T): FluentLoggerMethod.Builder = {
      val message = implicitly[ToMessage[T]].toMessage(instance)
      copy(m = m + message)
    }

    override def argument[T: ToArguments](instance: => T): FluentLoggerMethod.Builder = {
      val arguments = implicitly[ToArguments[T]].toArguments(instance)
      copy(args = args ++ arguments)
    }

    override def cause(e: Throwable): FluentLoggerMethod.Builder = copy(e = Some(e))

    override def log(): Unit = {
      val statement = Statement(markers = mkrs, message = m, arguments = args, e)
      apply(statement)
    }

    override def logWithPlaceholders(): Unit = {
      val statement = Statement(markers = mkrs, message = m.withPlaceHolders(args), arguments = args, e)
      apply(statement)
    }
  }

  object BuilderImpl {
    def empty: BuilderImpl = BuilderImpl(Markers.empty, Message.empty, Arguments.empty, None)
  }

  override def argument[T: ToArguments](instance: => T): FluentLoggerMethod.Builder = {
    BuilderImpl.empty.argument(instance)
  }

  override def cause(e: Throwable): FluentLoggerMethod.Builder = BuilderImpl.empty.cause(e)

  override def message[T: ToMessage](instance: => T): FluentLoggerMethod.Builder = {
    BuilderImpl.empty.message(instance)
  }

  override def marker[T: ToMarkers](instance: => T): FluentLoggerMethod.Builder = BuilderImpl.empty.marker(instance)

  override def apply[T: ToStatement](instance: => T)(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
    val statement = implicitly[ToStatement[T]].toStatement(instance)
    val markers           = collateMarkers(statement.markers)
    if (isEnabled(markers)) {
      parameterList.executeStatement(statement.withMarkers(markers))
    }
  }

  protected def collateMarkers(markers: Markers)(implicit line: Line, file: File, enclosing: Enclosing): Markers = {
    val sourceMarkers = logger.sourceInfoMarker(level, line, file, enclosing)
    sourceMarkers ++ markerState ++ markers
  }

  protected def isEnabled(markers: Markers): Boolean = {
    if (markers.nonEmpty) {
      parameterList.executePredicate(markers.marker)
    } else {
      parameterList.executePredicate()
    }
  }
}
