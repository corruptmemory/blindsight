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

package example.functional

import com.tersesystems.blindsight.slf4j.Logger

object FunctionalMain {

  def main(args: Array[String]): Unit = {
    val underlying = org.slf4j.LoggerFactory.getLogger(getClass)
    val logger: Logger = Logger(underlying)

    import cats.implicits._
    import treelog.LogTreeSyntaxWithoutAnnotations._

    // treelog using cats
    // https://github.com/lancewalton/treelog
    // Should print out each individual label at trace level with a marker
    // indicating the computation, then produce the result at debug level.
    // https://github.com/lancewalton/treelog/blob/master/src/test/scala/QuadraticRootsExample.scala

    // Should be able to wedge it into SLF4J using this:
    // https://github.com/oranda/treelog-scalajs/blob/master/src/main/scala/com/oranda/treelogui/LogTreeItem.scala
    val oneA: DescribedComputation[Int] = 1 ~> (v => s"The value is $v")
    oneA.value.run match {
      case (logTree, value) =>
        logger.trace(logTree.show)
        logger.info(s"result is ${value.show}")
    }

    // Also possible
    //val written = oneA.value.written
    //logger.info(written.show)
  }

}
