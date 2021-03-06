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

import org.slf4j.{Marker, MarkerFactory}

trait ToMarkers[T] {
  def toMarkers(instance: T): Markers
}

trait LowPriorityToMarkers {
  implicit val stringToMarker: ToMarkers[String] = (instance: String) =>
    Markers(MarkerFactory.getMarker(instance))
  implicit val markerToMarkers: ToMarkers[Marker] = (instance: Marker) => Markers(instance)
}

object ToMarkers extends LowPriorityToMarkers {
  def apply[T](f: T => Markers): ToMarkers[T] = f(_)
}
