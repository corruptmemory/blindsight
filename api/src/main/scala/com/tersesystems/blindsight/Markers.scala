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

import org.slf4j.{Marker, MarkerFactory}

import scala.collection.immutable
import scala.collection.immutable.StrictOptimizedSetOps

final class Markers private (internal: Set[Marker])
    extends immutable.Set[Marker]
    with StrictOptimizedSetOps[Marker, Set, Set[Marker]] {
  override def incl(elem: Marker): Set[Marker] = (internal.incl(elem))
  override def excl(elem: Marker): Set[Marker] = (internal.excl(elem))
  override def contains(elem: Marker): Boolean = internal.contains(elem)
  override def iterator: Iterator[Marker]      = internal.iterator

  lazy val marker: Marker = {
    val init = MarkerFactory.getDetachedMarker(internal.toString())
    internal.foldLeft(init) { (acc, el) => acc.add(el); acc; }
  }

  def toStatement: Statement = Statement().withMarkers(this)
}

object Markers {
  implicit val markersToMarkers: ToMarkers[Markers] = ToMarkers((instance: Markers) => instance)

  implicit def setToMarkers(set: Set[Marker]): Markers = Markers(set)

  def empty: Markers = new Markers(Set.empty)

  def apply[T: ToMarkers](instance: => T): Markers = implicitly[ToMarkers[T]].toMarkers(instance)

  def apply(markers: Set[Marker]): Markers = new Markers(markers)
  def apply(elements: Marker*): Markers    = new Markers(Set(elements: _*))
  def apply(marker: Marker): Markers       = new Markers(Set(marker))
}
