package example.slf4j.conditional

import java.util.concurrent.atomic.AtomicBoolean

import com.tersesystems.blindsight.Markers
import com.tersesystems.blindsight.fluent.SLF4JFluentLogger
import com.tersesystems.blindsight.slf4j.SLF4JLogger
import example.fluent.conditional.ConditionalFluentLogger
import org.slf4j.LoggerFactory

object ConditionalMain {

  def main(args: Array[String]): Unit = {
    val latch = new AtomicBoolean(true)
    def test: Boolean = {
      //println("test called at " + System.currentTimeMillis())
      latch.getAndSet(!latch.get())
    }
    val underlying  = LoggerFactory.getLogger(getClass)
    val slf4jLogger = new SLF4JLogger(underlying, Markers.empty)
    val logger      = new ConditionalFluentLogger(test, new SLF4JFluentLogger(slf4jLogger))

    logger.info
      .message("hello world, I render fine at")
      .argument(System.currentTimeMillis().toString)
      .logWithPlaceholders()
    logger.info
      .message("hello world, I do not render at all at {}")
      .argument(System.currentTimeMillis().toString)
      .logWithPlaceholders()
    logger.info
      .message("hello world, I render fine at")
      .argument(System.currentTimeMillis().toString)
      .logWithPlaceholders()
    logger.info
      .message("hello world, I do not render at all at {}")
      .argument(System.currentTimeMillis().toString)
      .logWithPlaceholders()

    var counter: Int = 0
    def mod4 = {
      //println("mod4 called at " + System.currentTimeMillis())
      counter = (counter + 1) % 2
      counter == 1
    }

    val moreLogger = logger.onCondition(mod4)
    moreLogger.info.message("1 {}").argument(System.currentTimeMillis().toString).log()
    moreLogger.info.message("2 {}").argument(System.currentTimeMillis().toString).log()
    moreLogger.info.message("3 {}").argument(System.currentTimeMillis().toString).log()
    moreLogger.info.message("4 {}").argument(System.currentTimeMillis().toString).log()
    moreLogger.info.message("5 {}").argument(System.currentTimeMillis().toString).log()
  }
}
