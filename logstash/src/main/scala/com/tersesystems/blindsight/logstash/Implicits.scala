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

package com.tersesystems.blindsight.logstash

import com.tersesystems.blindsight.{Arguments, Markers, ToArguments, ToMarkers}
import net.logstash.logback.argument.{StructuredArgument, StructuredArguments}
import net.logstash.logback.marker.{Markers => LogstashMarkers}

import scala.collection.JavaConverters._

trait LowPriorityMarkers {

  implicit val tupleStringToMarkers: ToMarkers[(String, String)] = ToMarkers {
    case (k, v) =>
      Markers(LogstashMarkers.append(k, v))
  }

  implicit val tupleBooleanToMarkers: ToMarkers[(String, Boolean)] = ToMarkers {
    case (k, v) =>
      Markers(LogstashMarkers.append(k, v))
  }

  implicit def tupleNumericToMarkers[T: Numeric]: ToMarkers[(String, T)] = ToMarkers {
    case (k, v) =>
      Markers(LogstashMarkers.append(k, v))
  }

  implicit def jsonToMarkers[T: ToJsonNode]: ToMarkers[(String, T)] = ToMarkers {
    case (k, instance) =>
      Markers {
        LogstashMarkers.defer { () =>
          val node = implicitly[ToJsonNode[T]].jsonNode(instance)
          LogstashMarkers.appendRaw(k, node.toPrettyString)
        }
      }
  }

  implicit def arrayToMarkers[T]: ToMarkers[(String, Seq[T])] = ToMarkers {
    case (k, v) =>
      Markers(LogstashMarkers.appendArray(k, v: _*))
  }

  implicit def mapToMarkers[T]: ToMarkers[Map[String, T]] = ToMarkers { map =>
    Markers(LogstashMarkers.appendEntries(map.asJava))
  }

}

trait LowPriorityToArguments {
  implicit val argToArguments: ToArguments[StructuredArgument] = ToArguments(Arguments(_))

  implicit val iterableArgToArguments: ToArguments[IterableOnce[StructuredArgument]] = ToArguments(
    Arguments(_)
  )

  implicit val kvStringToArguments: ToArguments[(String, String)] = ToArguments {
    case (k, v) =>
      Arguments(StructuredArguments.keyValue(k, v))
  }

  implicit val kvBooleanToArguments: ToArguments[(String, Boolean)] = ToArguments {
    case (k, v) =>
      Arguments(StructuredArguments.keyValue(k, v))
  }

  implicit def numericToArguments[T: Numeric]: ToArguments[(String, T)] = ToArguments {
    case (k, v) =>
      Arguments(StructuredArguments.keyValue(k, v))
  }

  implicit def jsonToArguments[T: ToJsonNode]: ToArguments[(String, T)] = ToArguments {
    case (k, instance) =>
      val node = implicitly[ToJsonNode[T]].jsonNode(instance)
      Arguments(StructuredArguments.raw(k, node.toPrettyString))
  }

  implicit def arrayToArguments[T]: ToArguments[(String, Seq[T])] = ToArguments {
    case (k, v) =>
      Arguments(StructuredArguments.array(k, v: _*))
  }

  implicit def mapToArguments[T]: ToArguments[Map[String, T]] = ToArguments { inputMap =>
    // Maps are problematic as they export in "{a=b}" format, and not the logfmt "a=b" format used by
    // other systems.  So let's break this down in to kv pairs instead.
    inputMap.foldLeft(Arguments.empty) { (acc, el) =>
      acc ++ Arguments(StructuredArguments.keyValue(el._1, el._2))
    }
  }

}

object LowPriorityToArguments extends LowPriorityToArguments

trait Implicits extends LowPriorityToArguments with LowPriorityMarkers

object Implicits extends Implicits
