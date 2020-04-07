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

/**
 * Type class for mapping to a statement.
 *
 * @tparam T the type
 */
trait ToStatement[T] {
  def toStatement(instance: => T): Statement
}

object ToStatement {
  def apply[T, S <: Statement](f: T => S): ToStatement[T] = f(_)

  implicit val statementToStatement: ToStatement[Statement] = ToStatement(identity)
}
