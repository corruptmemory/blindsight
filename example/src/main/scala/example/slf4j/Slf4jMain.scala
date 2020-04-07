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

package example.slf4j

import com.tersesystems.blindsight.api.{Arguments, Markers, ToMarkers}
import com.tersesystems.blindsight.slf4j._
import com.tersesystems.blindsight._
import org.slf4j.MarkerFactory

object Slf4jMain {

  final case class FeatureFlag(flagName: String)

  object FeatureFlag {
    implicit val toMarkers: ToMarkers[FeatureFlag] = ToMarkers { instance =>
      Markers(MarkerFactory.getDetachedMarker(instance.flagName))
    }
  }

  def main(args: Array[String]): Unit = {
    val logger = LoggerFactory.getLogger(getClass)

    val featureFlag = FeatureFlag("flag.enabled")
    if (logger.isDebugEnabled(featureFlag)) {
      logger.debug("this is a test")
    }

    logger.info("hello world")

    val m1 = MarkerFactory.getMarker("M1")

    logger.info("a" -> "b {} {}", Arguments(42, 53))

    val m2   = MarkerFactory.getMarker("M2")
    val base = logger.marker(m1).marker(m2)
    base.info("I should have two markers")

    val onlyInfo = new SLF4JLoggerAPI.Info[base.Predicate, base.Method] {
      override type Self      = base.Self
      override type Predicate = base.Predicate
      override type Method    = base.Method

      override def isInfoEnabled: Predicate = base.isInfoEnabled
      override def info: Method             = base.info
    }
    onlyInfo.info("good")
  }
}
