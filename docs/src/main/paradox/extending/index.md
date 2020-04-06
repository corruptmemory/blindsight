

## Extensible API

Because of the way that Blindsight is constructed, it is very easy to extend and adapt Blindsight to your needs.  

### Entry / Exit Trace Logger

For example, here's a logger extended with entry and exit methods:

```scala
import com.tersesystems.blindsight._
import com.tersesystems.blindsight.slf4j._
import org.slf4j.MarkerFactory
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

class FlowLogger(protected val logger: Logger)
    extends LoggerAPI.Proxy[LoggerPredicate, LoggerMethod] with Logger {
  override type Parent = Logger
  override type Self = FlowLogger

  private val entryLogger = logger.marker(MarkerFactory.getMarker("ENTRY"))
  private val exitLogger = logger.marker(MarkerFactory.getMarker("EXIT"))

  def entry: LoggerFlowMethod = {
    new LoggerFlowMethod {
      override def apply[B](message: String)(block: ExitHandle => B)(implicit enclosing: Enclosing): B = {
        entryLogger.trace(s"${enclosing.value} enter: $message")
        block(new ExitHandle {
          override def apply[T: ToStatement](instance: => T): T = {
            val result = instance
            val s = implicitly[ToStatement[T]].toStatement(result)
            exitLogger.trace(s"${enclosing.value} exit: result = ${s.message.toString}", s.arguments.asArray: _*)
            result
          }
        })
      }
    }
  }

  override def sourceInfoMarker(level: Level,
                                line: Line,
                                file: File,
                                enclosing: Enclosing): Markers = Markers.empty

  override def marker[T: ToMarkers](markerInstance: T) = new FlowLogger(logger.marker(markerInstance))

  override def markerState: Markers = logger.markerState
}

/**
 * This trait is an extension on the logger method that allows for
 * flow control using entry / exit handles with logging.
 */
trait LoggerFlowMethod {

  trait ExitHandle {
    def apply[T: ToStatement](instance: => T): T
  }

  def apply[B](message: String)(block: ExitHandle => B)(implicit enclosing: Enclosing): B
}

object FlowMain {

  def main(args: Array[String]): Unit = {
    val underlying = org.slf4j.LoggerFactory.getLogger(getClass)
    val logger = new FlowLogger(new SLF4JLogger(underlying, Markers.empty))

    logger.info("About to execute flow")

    // Need to specify to statement type class for the exit type:
    implicit val toIntStatement: ToStatement[Int] = ToStatement(i => Statement().withMessage(i.toString))
    val result: Int = logger.entry("entering") { exit =>
      exit(1 + 2)
    }
  }
}
```

In flat file format:

```
FgEdhil1mYM6O0Qbm7EAAA 2020-04-05T23:13:38.958+0000 [INFO ] example.flow.FlowMain$ in main  - About to execute flow
FgEdhil1mdQ6O0Qbm7EAAA 2020-04-05T23:13:39.039+0000 [TRACE] example.flow.FlowMain$ in main Set(ENTRY) [ ENTRY ] - example.flow.FlowMain.main result enter: entering
FgEdhil1md06O0Qbm7EAAA 2020-04-05T23:13:39.048+0000 [TRACE] example.flow.FlowMain$ in main Set(EXIT) [ EXIT ] - example.flow.FlowMain.main result exit: result = 3
```

JSON:

```json
{"id":"FgEdhil1mYM6O0Qbm7EAAA","relative_ns":-285601,"tse_ms":1586128418958,"start_ms":null,"@timestamp":"2020-04-05T23:13:38.958Z","@version":"1","message":"About to execute flow","logger_name":"example.flow.FlowMain$","thread_name":"main","level":"INFO","level_value":20000}
{"id":"FgEdhil1mdQ6O0Qbm7EAAA","relative_ns":80095000,"tse_ms":1586128419039,"start_ms":null,"@timestamp":"2020-04-05T23:13:39.039Z","@version":"1","message":"example.flow.FlowMain.main result enter: entering","logger_name":"example.flow.FlowMain$","thread_name":"main","level":"TRACE","level_value":5000,"tags":["Set(ENTRY)","ENTRY"]}
{"id":"FgEdhil1md06O0Qbm7EAAA","relative_ns":88443600,"tse_ms":1586128419048,"start_ms":null,"@timestamp":"2020-04-05T23:13:39.048Z","@version":"1","message":"example.flow.FlowMain.main result exit: result = 3","logger_name":"example.flow.FlowMain$","thread_name":"main","level":"TRACE","level_value":5000,"tags":["Set(EXIT)","EXIT"]}
```

### Conditional Fluent Logger

That's a fairly simple example, so let's do something a bit more complex.  Here's a conditional fluent logger:

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