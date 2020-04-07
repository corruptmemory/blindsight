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

final class Message private (fragments: Seq[String]) {
  def withPlaceHolders(args: Arguments): Message = {
    new Message(fragments :+ args.placeholders)
  }

  def +(message: Message): Message = new Message(fragments :+ message.toString)

  override def toString: String = fragments.mkString(" ")

  def mkString(sep: String): String = fragments.mkString(sep)

  def toStatement: Statement = Statement().withMessage(this)
}

object Message {
  implicit val toMessage: ToMessage[Message] = ToMessage((instance: Message) => instance)

  def empty: Message = new Message(Seq.empty)

  def apply[T: ToMessage](instance: => T): Message = implicitly[ToMessage[T]].toMessage(instance)

  def apply(elements: Seq[String]): Message = new Message(elements)

  def apply(message: => String): Message = new Message(Seq(message))
}
