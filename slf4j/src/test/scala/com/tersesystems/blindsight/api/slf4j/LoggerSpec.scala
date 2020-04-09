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

package com.tersesystems.blindsight.api.slf4j

import com.tersesystems.blindsight.api.Markers
import com.tersesystems.blindsight.fixtures.OneContextPerTest
import com.tersesystems.blindsight.slf4j.SLF4JLogger
import com.tersesystems.blindsight.slf4j.SLF4JLogger.Impl
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.slf4j.event.Level
import org.slf4j.{Logger, MarkerFactory}
import sourcecode.{Enclosing, File, Line}

class LoggerSpec extends AnyWordSpec with Matchers with OneContextPerTest {
  protected class NoSourceSLF4JLogger(underlying: org.slf4j.Logger, markers: Markers = Markers.empty) extends Impl(underlying, markers) {
    override protected def self(underlying: Logger, markerState: Markers): SLF4JLogger = {
      new NoSourceSLF4JLogger(underlying, markerState)
    }

    override def sourceInfoMarker(level: Level, line: Line, file: File, enclosing: Enclosing): Markers = Markers.empty
  }

  "A logger with no state marker" when {
    "calling predicate" should {
      "call with no arguments" in {
        val logger = new NoSourceSLF4JLogger(loggerContext.getLogger("testing"))

        logger.isDebugEnabled() must be(true)
      }

      "call with a marker" in {
        val logger = new NoSourceSLF4JLogger(loggerContext.getLogger("testing"))

        val marker = MarkerFactory.getDetachedMarker("test1")
        logger.isDebugEnabled(marker) must be(true)
      }
    }

    "calling method" should {
      "call with message" in {
        val logger = new NoSourceSLF4JLogger(loggerContext.getLogger("testing"))
        logger.debug("hello world")

        val event = listAppender.list.get(0)
        event.getMessage must equal("hello world")
      }

      "call with message and argument" in {
        val logger = new NoSourceSLF4JLogger(loggerContext.getLogger("testing"))
        logger.debug("hello world", 42)

        val event = listAppender.list.get(0)
        event.getMessage must equal("hello world")
        event.getArgumentArray.apply(0) must equal(42)
      }

      "call with message and two arguments" in {
        val logger = new NoSourceSLF4JLogger(loggerContext.getLogger("testing"))
        logger.debug("hello world", 42, 1)

        val event = listAppender.list.get(0)
        event.getMessage must equal("hello world")
        event.getArgumentArray.apply(0) must equal(42)
        event.getArgumentArray.apply(1) must equal(1)
      }

      "call with message and several arguments" in {
        val logger = new NoSourceSLF4JLogger(loggerContext.getLogger("testing"))
        logger.debug("hello world", 42, 1, "322")

        val event = listAppender.list.get(0)
        event.getMessage must equal("hello world")
        event.getArgumentArray.apply(0) must equal(42)
        event.getArgumentArray.apply(1) must equal(1)
        event.getArgumentArray.apply(2) must equal("322")
      }

      "call with marker, message and argument" in {
        val logger = new NoSourceSLF4JLogger(loggerContext.getLogger("testing"))

        val marker = MarkerFactory.getDetachedMarker("MARKER")

        logger.debug(marker, "hello world", 42)

        val event = listAppender.list.get(0)
        event.getMessage must equal("hello world")
        event.getMarker.contains(marker) must be(true)
        event.getArgumentArray.apply(0) must equal(42)
      }

      "call with marker, message and two arguments" in {
        val logger = new NoSourceSLF4JLogger(loggerContext.getLogger("testing"))

        val marker = MarkerFactory.getDetachedMarker("MARKER")

        logger.debug(marker, "hello world", 42, 1)

        val event = listAppender.list.get(0)
        event.getMessage must equal("hello world")
        event.getMarker.contains(marker) must be(true)
        event.getArgumentArray.apply(0) must equal(42)
        event.getArgumentArray.apply(1) must equal(1)
      }

      "call with marker, message and several arguments" in {
        val logger = new NoSourceSLF4JLogger(loggerContext.getLogger("testing"))

        val marker = MarkerFactory.getDetachedMarker("MARKER")

        logger.debug(marker, "hello world", 42, 1, "332")

        val event = listAppender.list.get(0)
        event.getMessage must equal("hello world")
        event.getMarker.contains(marker) must be(true)
        event.getArgumentArray.apply(0) must equal(42)
        event.getArgumentArray.apply(1) must equal(1)
        event.getArgumentArray.apply(2) must equal("332")
      }
    }
  }

  "logger with state marker" should {
    "calling predicate" should {
      "call with no arguments" in {
        val nomarkerLogger        = new NoSourceSLF4JLogger(loggerContext.getLogger("testing"))
        val marker                = MarkerFactory.getDetachedMarker("DENY_MARKER")
        val loggerWithStateMarker = nomarkerLogger.withMarker(marker)

        // We were true before, so if false it's because the DENY_MARKER was hit
        loggerWithStateMarker.isDebugEnabled() must be(false)
      }

      "call with marker argument" in {
        val nomarkerLogger        = new NoSourceSLF4JLogger(loggerContext.getLogger("testing"))
        val marker                = MarkerFactory.getDetachedMarker("DENY_MARKER")
        val loggerWithStateMarker = nomarkerLogger.withMarker(marker)

        val childMarker = MarkerFactory.getDetachedMarker("CHILD_MARKER")

        // We were true before, so if false it's because the DENY_MARKER was hit
        loggerWithStateMarker.isDebugEnabled(childMarker) must be(false)
      }
    }

    "call method" should {

      "call with message and argument" in {
        val nomarkerLogger = new NoSourceSLF4JLogger(loggerContext.getLogger("testing"))

        val marker                = MarkerFactory.getDetachedMarker("MARKER")
        val loggerWithStateMarker = nomarkerLogger.withMarker(marker)

        // No explicit marker...
        loggerWithStateMarker.debug("hello world", 42)

        val event = listAppender.list.get(0)
        val m     = event.getMarker
        m.contains(marker) must be(true)
      }

      "call with marker, message and argument" in {
        val nomarkerLogger = new NoSourceSLF4JLogger(loggerContext.getLogger("testing"))

        val marker1               = MarkerFactory.getDetachedMarker("MARKER1")
        val loggerWithStateMarker = nomarkerLogger.withMarker(marker1)

        // No explicit marker...
        val marker2 = MarkerFactory.getDetachedMarker("MARKER2")
        loggerWithStateMarker.debug(marker2, "hello world", 42)

        val event  = listAppender.list.get(0)
        val marker = event.getMarker
        marker.contains(marker1) must be(true)
        marker.contains(marker2) must be(true)
      }

      "call with marker, message and two arguments" in {
        val nomarkerLogger = new NoSourceSLF4JLogger(loggerContext.getLogger("testing"))

        val marker1               = MarkerFactory.getDetachedMarker("MARKER1")
        val loggerWithStateMarker = nomarkerLogger.withMarker(marker1)

        // No explicit marker...
        val marker2 = MarkerFactory.getDetachedMarker("MARKER2")
        loggerWithStateMarker.debug(marker2, "hello world", 42, 1)

        val event  = listAppender.list.get(0)
        val marker = event.getMarker
        marker.contains(marker1) must be(true)
        marker.contains(marker2) must be(true)

        event.getArgumentArray.apply(0) must equal(42)
        event.getArgumentArray.apply(1) must equal(1)
      }

      "call with marker, message and several arguments" in {
        val nomarkerLogger = new NoSourceSLF4JLogger(loggerContext.getLogger("testing"))

        val marker1               = MarkerFactory.getDetachedMarker("MARKER1")
        val loggerWithStateMarker = nomarkerLogger.withMarker(marker1)

        // No explicit marker...
        val marker2 = MarkerFactory.getDetachedMarker("MARKER2")
        loggerWithStateMarker.debug(marker2, "hello world", 42, 1, "332")

        val event  = listAppender.list.get(0)
        val marker = event.getMarker
        marker.contains(marker1) must be(true)
        marker.contains(marker2) must be(true)

        event.getArgumentArray.apply(0) must equal(42)
        event.getArgumentArray.apply(1) must equal(1)
        event.getArgumentArray.apply(2) must equal("332")

      }
    }
  }
}
