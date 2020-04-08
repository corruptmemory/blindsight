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

import org.slf4j.Logger

trait LoggerResolver[T] {
  def resolveLogger(instance: T): org.slf4j.Logger
}

trait LowPriorityLoggerResolverImplicits {
  implicit val stringToResolver: LoggerResolver[String] = (instance: String) => {
    val factory = org.slf4j.LoggerFactory.getILoggerFactory
    factory.getLogger(instance)
  }

  implicit def classToResolver[T]: LoggerResolver[Class[T]] = (instance: Class[T]) => {
    val factory = org.slf4j.LoggerFactory.getILoggerFactory
    factory.getLogger(instance.getName)
  }

  implicit val loggerToResolver: LoggerResolver[org.slf4j.Logger] =
    (instance: org.slf4j.Logger) => instance
}

object LoggerResolver extends LowPriorityLoggerResolverImplicits
