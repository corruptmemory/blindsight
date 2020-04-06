# Conditional Fluent Logger

Here's a conditional fluent logger:

```scala
import com.tersesystems.blindsight.ToStatement
import com.tersesystems.blindsight.fluent.{SLF4JFluentLogger, SLF4JFluentLoggerMethod}
import com.tersesystems.blindsight.slf4j.{LoggerAPI, LoggerPredicate}
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

class ConditionalFluentLogger(test: => Boolean, logger: SLF4JFluentLogger)
    extends LoggerAPI[LoggerPredicate, ConditionalFluentLoggerMethod] {
  override type Predicate = LoggerPredicate
  override type Method    = ConditionalFluentLoggerMethod
  override type Self      = ConditionalFluentLogger

  def onCondition(test2: Boolean): Self = new ConditionalFluentLogger(test && test2, logger)

  override def isTraceEnabled: Predicate = logger.isTraceEnabled
  override def trace: Method             = new ConditionalFluentLoggerMethod(Level.TRACE, test, logger)

  override def isDebugEnabled: Predicate = logger.isDebugEnabled
  override def debug: Method             = new ConditionalFluentLoggerMethod(Level.DEBUG, test, logger)

  override def isInfoEnabled: Predicate = logger.isInfoEnabled
  override def info: Method             = new ConditionalFluentLoggerMethod(Level.INFO, test, logger)

  override def isWarnEnabled: Predicate = logger.isWarnEnabled
  override def warn: Method             = new ConditionalFluentLoggerMethod(Level.WARN, test, logger)

  override def isErrorEnabled: Predicate = logger.isErrorEnabled
  override def error: Method             = new ConditionalFluentLoggerMethod(Level.ERROR, test, logger)
}

class ConditionalFluentLoggerMethod(level: Level, test: => Boolean, logger: SLF4JFluentLogger)
    extends SLF4JFluentLoggerMethod(level, logger) {

  override def apply[T: ToStatement](
      instance: => T
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
    if (test) {
      val statement = implicitly[ToStatement[T]].toStatement(instance)
      logger.parameterList(level).executeStatement(statement)
    }
  }
}

object ConditionalFluentMain {

  def main(args: Array[String]): Unit = {
    val latch = new AtomicBoolean(true)
    def test: Boolean = {
      println("test called at " + System.currentTimeMillis())
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
      println("mod4 called at " + System.currentTimeMillis())
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
```

This renders:

```
test called at 1586136547249
FgEdhioD88pOjtEG5uxAAA 18:29:07.251 [INFO ] e.s.c.ConditionalMain$ -  hello world, I render fine at 1586136547243
test called at 1586136547339
test called at 1586136547339
FgEdhioD899nR2iDc3YgAA 18:29:07.339 [INFO ] e.s.c.ConditionalMain$ -  hello world, I render fine at 1586136547339
test called at 1586136547339
test called at 1586136547340
mod4 called at 1586136547340
FgEdhioD9ACdHaINzdiAAA 18:29:07.340 [INFO ] e.s.c.ConditionalMain$ -  1 1586136547340
test called at 1586136547341
test called at 1586136547341
mod4 called at 1586136547341
test called at 1586136547341
test called at 1586136547341
mod4 called at 1586136547341
FgEdhioD9ACdHaINzdiAAB 18:29:07.341 [INFO ] e.s.c.ConditionalMain$ -  5 1586136547341
```