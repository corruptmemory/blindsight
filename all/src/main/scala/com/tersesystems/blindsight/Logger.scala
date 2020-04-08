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

package com.tersesystems.blindsight

import com.tersesystems.blindsight.api._
import com.tersesystems.blindsight.fluent.FluentLogger
import com.tersesystems.blindsight.semantic.SemanticLogger
import com.tersesystems.blindsight.slf4j.SLF4JLogger

trait Logger extends SLF4JLogger {
  def underlying: org.slf4j.Logger
  def fluent: FluentLogger
  def refine[MessageType]: SemanticLogger[MessageType]
}

trait LoggerFactory {
  def getLogger[T: LoggerResolver](instance: T): Logger
}

object LoggerFactory {
  import java.util.ServiceLoader
  private val loggerFactoryLoader = ServiceLoader.load(classOf[LoggerFactory])

  private lazy val loggerFactory: LoggerFactory = {
    import javax.management.ServiceNotFoundException

    import scala.collection.JavaConverters._

    loggerFactoryLoader.iterator().asScala.find(_ != null).getOrElse {
      throw new ServiceNotFoundException("No logger factory found!")
    }
  }

  def getLogger[T: LoggerResolver](instance: => T): Logger = {
    loggerFactory.getLogger(instance)
  }
}
