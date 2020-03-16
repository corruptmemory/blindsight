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

package com.tersesystems.blindsight

import org.slf4j.Marker

final case class Statement(
    markers: Markers,
    message: Message,
    arguments: Arguments,
    throwable: Option[Throwable]
) {

  def withArguments[T: ToArguments](args: T): Statement = {
    copy(arguments = implicitly[ToArguments[T]].toArguments(args))
  }

  def withMarkers[T: ToMarkers](markers: T): Statement = {
    copy(markers = implicitly[ToMarkers[T]].toMarkers(markers))
  }

  def withMessage[T: ToMessage](message: T): Statement = {
    copy(message = implicitly[ToMessage[T]].toMessage(message))
  }

  def withThrowable(t: Throwable): Statement = {
    copy(throwable = Some(t))
  }
}

object Statement {

  def apply(): Statement = Statement(Markers.empty, Message.empty, Arguments.empty, None)

//  def string(f: => String): Statement =
//    Statement(Markers.empty, Message(f), Arguments.empty, None)
//
//  def stringArg(f: => (String, _)): Statement =
//    Statement(Markers.empty, Message(f._1), Arguments(f._2), None)
//
//  def stringArg1Arg2(f: => (String, _, _)): Statement =
//    Statement(Markers.empty, Message(f._1), Arguments(f._2, f._3), None)
//
//  def stringArray(f: => (String, Seq[_])): Statement =
//    Statement(Markers.empty, Message(f._1), Arguments(f._2), None)
//
//  def markerString(f: => (Marker, String)): Statement =
//    Statement(Markers(f._1), Message(f._2), Arguments.empty, None)
//
//  def markerStringArg(f: => (Marker, String, _)): Statement =
//    Statement(Markers(f._1), Message(f._2), Arguments(f._3), None)
//
//  def markerStringArg1Arg2(f: (Marker, String, _, _)): Statement =
//    Statement(Markers(f._1), Message(f._2), Arguments(f._3, f._4), None)
//
//  def markerStringArray(f: (Marker, String, Seq[_])): Statement =
//    Statement(Markers(f._1), Message(f._2), Arguments(f._3), None)
}
