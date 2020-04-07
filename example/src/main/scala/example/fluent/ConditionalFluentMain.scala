package example.fluent

import java.util.concurrent.atomic.AtomicBoolean

import com.tersesystems.blindsight.LoggerFactory
import com.tersesystems.blindsight.fluent.FluentLogger

object ConditionalFluentMain {

  def main(args: Array[String]): Unit = {
    val latch = new AtomicBoolean(true)
    def test: Boolean = {
      //println("test called at " + System.currentTimeMillis())
      latch.getAndSet(!latch.get())
    }
    val logger: FluentLogger = LoggerFactory.getLogger(getClass).fluent.onCondition(test)

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
