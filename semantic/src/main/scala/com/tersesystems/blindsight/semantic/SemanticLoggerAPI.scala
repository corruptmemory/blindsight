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

package com.tersesystems.blindsight.semantic

import com.tersesystems.blindsight.ToStatement

trait SemanticLoggerComponent[MessageType, P, M[_]] {
  type Predicate = P
  type Method[T] = M[T]
  type Self[T]
}

trait SemanticRefineMixin[MessageType] {
  type Self[T]

  def refine[T <: MessageType: ToStatement]: Self[T]
}

trait SemanticLoggerAPI[MessageType, P, M[_]]
    extends SemanticLoggerComponent[MessageType, P, M]
    with TraceSemanticLoggerAPI[MessageType, P, M]
    with DebugSemanticLoggerAPI[MessageType, P, M]
    with InfoSemanticLoggerAPI[MessageType, P, M]
    with WarnSemanticLoggerAPI[MessageType, P, M]
    with ErrorSemanticLoggerAPI[MessageType, P, M]

object SemanticLoggerAPI {
  trait Proxy[BaseType, P, M[_]] extends SemanticLoggerAPI[BaseType, P, M] {
    protected def logger: SemanticLoggerAPI[BaseType, Predicate, Method]

    override def isTraceEnabled: Predicate = logger.isTraceEnabled
    override def trace: Method[BaseType]   = logger.trace

    override def isDebugEnabled: Predicate = logger.isDebugEnabled
    override def debug: Method[BaseType]   = logger.debug

    override def isInfoEnabled: Predicate = logger.isInfoEnabled
    override def info: Method[BaseType]   = logger.info

    override def isWarnEnabled: Predicate = logger.isWarnEnabled
    override def warn: Method[BaseType]   = logger.warn

    override def isErrorEnabled: Predicate = logger.isErrorEnabled
    override def error: Method[BaseType]   = logger.error
  }
}

/**
 * This trait defines only "isTraceLogging" and "trace" methods.
 */
trait TraceSemanticLoggerAPI[MessageType, P, M[_]]
    extends SemanticLoggerComponent[MessageType, P, M] {
  def isTraceEnabled: Predicate
  def trace: Method[MessageType]
}

/**
 * This trait defines only "isDebugLogging" and "debug" methods.
 */
trait DebugSemanticLoggerAPI[MessageType, P, M[_]]
    extends SemanticLoggerComponent[MessageType, P, M] {
  def isDebugEnabled: Predicate
  def debug: Method[MessageType]
}

/**
 * This trait defines only "isInfoLogging" and "info" methods.
 */
trait InfoSemanticLoggerAPI[MessageType, P, M[_]]
    extends SemanticLoggerComponent[MessageType, P, M] {
  def isInfoEnabled: Predicate
  def info: Method[MessageType]
}

/**
 * This trait defines only "isWarnLogging" and "warn" methods.
 */
trait WarnSemanticLoggerAPI[MessageType, P, M[_]]
    extends SemanticLoggerComponent[MessageType, P, M] {
  def isWarnEnabled: Predicate
  def warn: Method[MessageType]
}

/**
 * This trait defines only "isErrorLogging" and "error" methods.
 */
trait ErrorSemanticLoggerAPI[MessageType, P, M[_]]
    extends SemanticLoggerComponent[MessageType, P, M] {
  def isErrorEnabled: Predicate
  def error: Method[MessageType]
}
