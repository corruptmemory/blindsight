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

trait LoggerComponent[P, M] {
  type Predicate <: P
  type Method <: M
  type Self
}

/**
 * This trait defines an SLF4J compatible logger with all five levels of logging.
 */
trait LoggerAPI[P, M]
    extends LoggerComponent[P, M]
    with TraceLoggerAPI[P, M]
    with DebugLoggerAPI[P, M]
    with InfoLoggerAPI[P, M]
    with WarnLoggerAPI[P, M]
    with ErrorLoggerAPI[P, M]

object LoggerAPI {
  trait Proxy[P, M] extends LoggerAPI[P, M] {
    type Parent <: LoggerAPI[P, M]

    protected val logger: Parent

    override type Method    = logger.Method
    override type Predicate = logger.Predicate

    override def isTraceEnabled: Predicate = logger.isTraceEnabled
    override def trace: Method             = logger.trace

    override def isDebugEnabled: Predicate = logger.isDebugEnabled
    override def debug: Method             = logger.debug

    override def isInfoEnabled: Predicate = logger.isInfoEnabled
    override def info: Method             = logger.info

    override def isWarnEnabled: Predicate = logger.isWarnEnabled
    override def warn: Method             = logger.warn

    override def isErrorEnabled: Predicate = logger.isErrorEnabled
    override def error: Method             = logger.error
  }
}

/**
 * This trait defines only "isTraceLogging" and "trace" methods.
 */
trait TraceLoggerAPI[P, M] extends LoggerComponent[P, M] {
  def isTraceEnabled: Predicate
  def trace: Method
}

/**
 * This trait defines only "isDebugLogging" and "debug" methods.
 */
trait DebugLoggerAPI[P, M] extends LoggerComponent[P, M] {
  def isDebugEnabled: Predicate
  def debug: Method
}

/**
 * This trait defines only "isInfoLogging" and "info" methods.
 */
trait InfoLoggerAPI[P, M] extends LoggerComponent[P, M] {
  def isInfoEnabled: Predicate
  def info: Method
}

/**
 * This trait defines only "isWarnLogging" and "warn" methods.
 */
trait WarnLoggerAPI[P, M] extends LoggerComponent[P, M] {
  def isWarnEnabled: Predicate
  def warn: Method
}

/**
 * This trait defines only "isErrorLogging" and "error" methods.
 */
trait ErrorLoggerAPI[P, M] extends LoggerComponent[P, M] {
  def isErrorEnabled: Predicate
  def error: Method
}
