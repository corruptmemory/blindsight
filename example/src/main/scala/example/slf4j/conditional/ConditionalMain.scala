package example.slf4j.conditional

import java.util.concurrent.atomic.AtomicBoolean

import com.tersesystems.blindsight.Markers
import com.tersesystems.blindsight.slf4j.SLF4JLogger
import org.slf4j.LoggerFactory

object ConditionalMain {

  def main(args: Array[String]): Unit = {
    val latch = new AtomicBoolean(true)
    def test: Boolean = {
      println("test called at " + System.currentTimeMillis())
      latch.getAndSet(! latch.get())
    }
    val underlying = LoggerFactory.getLogger(getClass)
    val logger = new ConditionalLogger(test, new SLF4JLogger(underlying, Markers.empty))

    logger.info("hello world, I render fine at {}", System.currentTimeMillis())
    logger.info("hello world, I do not render at all at {}", System.currentTimeMillis())
    logger.info("hello world, I render fine at {}", System.currentTimeMillis())
    logger.info("hello world, I do not render at all at {}", System.currentTimeMillis())

    var counter: Int = 0
    def mod4 = {
      println("mod4 called at " + System.currentTimeMillis())
      counter = (counter + 1) % 2
      counter == 1
    }

    val moreLogger = logger.onCondition(mod4)
    moreLogger.info("1 {}", System.currentTimeMillis())
    moreLogger.info("2 {}", System.currentTimeMillis())
    moreLogger.info("3 {}", System.currentTimeMillis())
    moreLogger.info("4 {}", System.currentTimeMillis())
    moreLogger.info("5 {}", System.currentTimeMillis())

  }
}
