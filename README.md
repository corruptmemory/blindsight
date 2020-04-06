# Blindsight

Blindsight is a Scala logging API that allows for fluent logging, semantic logging, and context aware logging.
 
The name is taken from Peter Watt's excellent first contact novel, [Blindsight](https://en.wikipedia.org/wiki/Blindsight_\(Watts_novel\)).

## Documentation 

See the documentation for more details.

## Overview

Blindsight breaks down logging statements into component parts through type classes:

* `api` - `Markers`, `Message`, and `Arguments`, which compose to `Statement` through type class patterns.
* `logstash` - Type class bindings of `Markers` and `Arguments` to [Logstash Markers and StructuredArguments](https://github.com/logstash/logstash-logback-encoder#event-specific-custom-fields).

Blindsight provides three different logging API: 

* `slf4j` - a straight reimplementation of the SLF4J API, designed for drop in replacements.
* `fluent` - a fluent builder API using provided type classes where logging statements are built progressively.
* `semantic` - a strongly typed API relying on user provided type classes that is well-suited for rich domain events.
  
Blindsight is designed to defer layout decisions to the backend logging framework, so you can write the same logging event out to a text file, console, to a database, and to JSON on the backend.  Having said that, you are strongly encouraged to use [logstash-logback-encoder](https://github.com/logstash/logstash-logback-encoder) and [Terse Logback](https://tersesystems.github.io/terse-logback/).

This doesn't mean very much by itself, so let's show examples.

## SLF4J API

```scala
import com.tersesystems.blindsight._
import com.tersesystems.blindsight.slf4j.{InfoLoggerAPI, Logger}
import org.slf4j.MarkerFactory

object Slf4jMain {

  final case class FeatureFlag(flagName: String)

  object FeatureFlag {
    implicit val toMarkers: ToMarkers[FeatureFlag] = ToMarkers { instance =>
      Markers(MarkerFactory.getDetachedMarker(instance.flagName))
    }
  }

  def main(args: Array[String]): Unit = {
    val underlying = org.slf4j.LoggerFactory.getLogger(getClass)
    val logger: Logger = Logger(underlying)

    val featureFlag = FeatureFlag("flag.enabled")
    if (logger.isDebugEnabled(featureFlag)) {
      logger.debug("this is a test")
    }

    logger.info("hello world")

    val m1 = MarkerFactory.getMarker("M1")
    val m2 = MarkerFactory.getMarker("M2")
    val base = logger.marker(m1).marker(m2)
    base.info("I should have two markers")

    val onlyInfo = new InfoLoggerAPI[base.Predicate, base.Method] {
      override type Self = base.Self
      override type Predicate = base.Predicate
      override type Method = base.Method

      override def isInfoEnabled: Predicate = base.isInfoEnabled
      override def info: Method = base.info
    }
    onlyInfo.info("good")
  }
}
```

produces the following in `application.log`:

```
FgEdh9F8h8EHR2iDc3YgAA 2020-04-05T18:27:08.435+0000 [DEBUG] example.slf4j.Slf4jMain$ in main  - this is a test
FgEdh9F8h8GnR2iDc3YgAA 2020-04-05T18:27:08.440+0000 [INFO ] example.slf4j.Slf4jMain$ in main  - hello world
FgEdh9F8h8IHR2iDc3YgAA 2020-04-05T18:27:08.443+0000 [INFO ] example.slf4j.Slf4jMain$ in main Set(M1, M2) [ M1, M2 ] - I should have two markers
FgEdh9F8h8InR2iDc3YgAA 2020-04-05T18:27:08.444+0000 [INFO ] example.slf4j.Slf4jMain$ in main Set(M1, M2) [ M1, M2 ] - good
```

and the following JSON:

```json
{"id":"FgEdh9F8h8EHR2iDc3YgAA","relative_ns":-365200,"tse_ms":1586111228435,"start_ms":null,"@timestamp":"2020-04-05T18:27:08.435Z","@version":"1","message":"this is a test","logger_name":"example.slf4j.Slf4jMain$","thread_name":"main","level":"DEBUG","level_value":10000}
{"id":"FgEdh9F8h8GnR2iDc3YgAA","relative_ns":2997600,"tse_ms":1586111228440,"start_ms":null,"@timestamp":"2020-04-05T18:27:08.440Z","@version":"1","message":"hello world","logger_name":"example.slf4j.Slf4jMain$","thread_name":"main","level":"INFO","level_value":20000}
{"id":"FgEdh9F8h8IHR2iDc3YgAA","relative_ns":6442600,"tse_ms":1586111228443,"start_ms":null,"@timestamp":"2020-04-05T18:27:08.443Z","@version":"1","message":"I should have two markers","logger_name":"example.slf4j.Slf4jMain$","thread_name":"main","level":"INFO","level_value":20000,"tags":["Set(M1, M2)","M1","M2"]}
{"id":"FgEdh9F8h8InR2iDc3YgAA","relative_ns":7005600,"tse_ms":1586111228444,"start_ms":null,"@timestamp":"2020-04-05T18:27:08.444Z","@version":"1","message":"good","logger_name":"example.slf4j.Slf4jMain$","thread_name":"main","level":"INFO","level_value":20000,"tags":["Set(M1, M2)","M1","M2"]}
```

## Fluent API

The fluent API works against `Markers`, `Message`, and `Arguments`.  Here it's being used with the `logstash` binding:

```scala
package example.fluent

import com.tersesystems.blindsight.fluent.FluentLogger
import com.tersesystems.blindsight.logstash.Implicits._

object Main {

  val underlying = org.slf4j.LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    import com.fasterxml.jackson.databind.ObjectMapper
    val json = """{ "f1" : "v1" }"""
    val objectMapper = new ObjectMapper
    val jsonNode = objectMapper.readTree(json)

    FluentLogger(underlying).info
      .marker("string" -> "steve")
      .marker("array" -> Seq("one", "two", "three"))
      .marker("markerJson" -> jsonNode)
      .marker("number" -> 42)
      .marker("boolean" -> true)
      .message("herp")
      .message("derp")
      .message("{}").argument("arg1" -> "value1")
      .message("{}").argument("numericArg" -> 42)
      .message("and then some more text")
      .message("{}").argument("booleanArg" -> false)
      .argument(Map("a" -> "b"))
      .argument("sequenceArg" -> Seq("a", "b", "c"))
      .log()
  }
}
```

produces the following to `application.log`:

```
FgEdh9F8SBhOjtEG5uxAAA 2020-04-05T18:32:56.427+0000 [INFO ] example.fluent.FluentMain$ in main HashSet(string=steve, number=42, array=[one, two, three], boolean=true, DEFERRED) [ LS_APPEND_OBJECT, LS_APPEND_OBJECT, LS_APPEND_OBJECT, LS_APPEND_OBJECT, DEFERRED ] - herp derp arg1=value1 numericArg=42 and then some more text booleanArg=false
```

and the following to `application.json`: 

```json
{"id":"FgEdh9F8SBhOjtEG5uxAAA","relative_ns":-356000,"tse_ms":1586111576427,"start_ms":null,"@timestamp":"2020-04-05T18:32:56.427Z","@version":"1","message":"herp derp arg1=value1 numericArg=42 and then some more text booleanArg=false","logger_name":"example.fluent.FluentMain$","thread_name":"main","level":"INFO","level_value":20000,"tags":["HashSet(string=steve, number=42, array=[one, two, three], boolean=true, DEFERRED)"],"string":"steve","number":42,"array":["one","two","three"],"boolean":true,"markerJson":{
  "f1" : "v1"
},"arg1":"value1","numericArg":42,"booleanArg":false,"a":"b","sequenceArg":["a","b","c"]}
```
 
## Semantic API

The semantic API works against `Statement` directly.  The application is expected to handle the type class mapping to `Statement`.

Here is an example:

```scala
import com.tersesystems.blindsight._
import com.tersesystems.blindsight.semantic.SemanticLogger
import net.logstash.logback.argument.StructuredArguments.kv

object SemanticMain {

  trait UserEvent {
    def name: String
  }

  final case class UserLoggedInEvent(name: String, ipAddr: String) extends UserEvent

  object UserLoggedInEvent {
    implicit val toMessage: ToMessage[UserLoggedInEvent] = ToMessage { instance =>
      Message(instance.toString)
    }

    implicit val toArguments: ToArguments[UserLoggedInEvent] = ToArguments { instance =>
      Arguments(
        kv("name", instance.name),
        kv("ipAddr", instance.ipAddr)
      )
    }

    implicit val toStatement: ToStatement[UserLoggedInEvent] = ToStatement { instance =>
      Statement().withMessage(instance).withArguments(instance)
    }
  }

  final case class UserLoggedOutEvent(name: String, reason: String) extends UserEvent

  object UserLoggedOutEvent {
    implicit val toMessage: ToMessage[UserLoggedOutEvent] = ToMessage { instance =>
      Message(instance.toString)
    }

    implicit val toArguments: ToArguments[UserLoggedOutEvent] = ToArguments { instance =>
      Arguments(
        kv("name", instance.name),
        kv("reason", instance.reason)
      )
    }

    implicit val toStatement: ToStatement[UserLoggedOutEvent] = ToStatement { instance =>
      Statement().withMessage(instance).withArguments(instance)
    }
  }

  def main(args: Array[String]): Unit = {
    val underlying = org.slf4j.LoggerFactory.getLogger(getClass)
    val userEventLogger: SemanticLogger[UserEvent] = SemanticLogger(underlying)
    userEventLogger.info(UserLoggedInEvent("steve", "127.0.0.1"))
    userEventLogger.info(UserLoggedOutEvent("steve", "timeout"))

    val onlyLoggedInEventLogger: SemanticLogger[UserLoggedInEvent] = userEventLogger.refine[UserLoggedInEvent]
    onlyLoggedInEventLogger.info(UserLoggedInEvent("mike", "10.0.0.1")) // won't work with logged out event
  }
}
```

in plain text:

```
FgEdhil2znw6O0Qbm7EAAA 2020-04-05T23:09:08.359+0000 [INFO ] example.semantic.SemanticMain$ in main  - UserLoggedInEvent(steve,127.0.0.1)
FgEdhil2zsg6O0Qbm7EAAA 2020-04-05T23:09:08.435+0000 [INFO ] example.semantic.SemanticMain$ in main  - UserLoggedOutEvent(steve,timeout)
FgEdhil2zsk6O0Qbm7EAAA 2020-04-05T23:09:08.436+0000 [INFO ] example.semantic.SemanticMain$ in main  - UserLoggedInEvent(mike,10.0.0.1)
```

and in JSON:

```json
{"id":"FgEdhil2znw6O0Qbm7EAAA","relative_ns":-298700,"tse_ms":1586128148359,"start_ms":null,"@timestamp":"2020-04-05T23:09:08.359Z","@version":"1","message":"UserLoggedInEvent(steve,127.0.0.1)","logger_name":"example.semantic.SemanticMain$","thread_name":"main","level":"INFO","level_value":20000,"name":"steve","ipAddr":"127.0.0.1"}
{"id":"FgEdhil2zsg6O0Qbm7EAAA","relative_ns":74895700,"tse_ms":1586128148435,"start_ms":null,"@timestamp":"2020-04-05T23:09:08.435Z","@version":"1","message":"UserLoggedOutEvent(steve,timeout)","logger_name":"example.semantic.SemanticMain$","thread_name":"main","level":"INFO","level_value":20000,"name":"steve","reason":"timeout"}
{"id":"FgEdhil2zsk6O0Qbm7EAAA","relative_ns":75837600,"tse_ms":1586128148436,"start_ms":null,"@timestamp":"2020-04-05T23:09:08.436Z","@version":"1","message":"UserLoggedInEvent(mike,10.0.0.1)","logger_name":"example.semantic.SemanticMain$","thread_name":"main","level":"INFO","level_value":20000,"name":"mike","ipAddr":"10.0.0.1"}
```

## Extensible API

Because of the way that Blindsight is constructed, it is very easy to extend and adapt Blindsight to your needs.  

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

And here's a conditional fluent logger:

```scala

class ConditionalLogger(test: => Boolean, protected val logger: Logger with ParameterListMixin)
    extends LoggerAPI[LoggerPredicate, ConditionalLoggerMethod] {
  override type Self      = ConditionalLogger
  override type Method    = ConditionalLoggerMethod
  override type Predicate = logger.Predicate

  def onCondition(test2: => Boolean): Self = new ConditionalLogger(test && test2, logger)

  override def isTraceEnabled: Predicate = logger.isTraceEnabled
  override def trace: Method             = new ConditionalLoggerMethod(Level.TRACE, test, logger)

  override def isDebugEnabled: Predicate = logger.isDebugEnabled
  override def debug: Method             = new ConditionalLoggerMethod(Level.DEBUG, test, logger)

  override def isInfoEnabled: Predicate = logger.isInfoEnabled
  override def info: Method             = new ConditionalLoggerMethod(Level.INFO, test, logger)

  override def isWarnEnabled: Predicate = logger.isWarnEnabled
  override def warn: Method             = new ConditionalLoggerMethod(Level.WARN, test, logger)

  override def isErrorEnabled: Predicate = logger.isErrorEnabled
  override def error: Method             = new ConditionalLoggerMethod(Level.ERROR, test, logger)
}

class ConditionalLoggerMethod(val level: Level,
                              test: => Boolean,
                              logger: Logger with ParameterListMixin)
    extends LoggerMethod {

  override def apply(message: String)(implicit line: Line, file: File, enclosing: Enclosing): Unit =
    if (test) {
      logger.parameterList(level).message(message)
    }

  override def apply(format: String,
                     arg: Any)(implicit line: Line, file: File, enclosing: Enclosing): Unit =
    if (test) {
      logger.parameterList(level).messageArg1(format, arg)
    }

  override def apply(format: String, arg1: Any, arg2: Any)(implicit line: Line,
                                                           file: File,
                                                           enclosing: Enclosing): Unit = if (test) {
    logger.parameterList(level).messageArg1Arg2(format, arg1, arg2)
  }

  override def apply(format: String,
                     args: Any*)(implicit line: Line, file: File, enclosing: Enclosing): Unit =
    if (test) {
      logger.parameterList(level).messageArgs(format, args)
    }

  override def apply(marker: Marker,
                     message: String)(implicit line: Line, file: File, enclosing: Enclosing): Unit =
    if (test) {
      logger.parameterList(level).markerMessage(marker, message)
    }

  override def apply(marker: Marker, format: String, arg: Any)(implicit line: Line,
                                                               file: File,
                                                               enclosing: Enclosing): Unit =
    if (test) {
      logger.parameterList(level).markerMessageArg1(marker, format, arg)
    }

  override def apply(marker: Marker, format: String, arg1: Any, arg2: Any)(
      implicit line: Line,
      file: File,
      enclosing: Enclosing): Unit = if (test) {
    logger.parameterList(level).markerMessageArg1Arg2(marker, format, arg1, arg2)
  }

  override def apply(marker: Marker, format: String, args: Any*)(implicit line: Line,
                                                                 file: File,
                                                                 enclosing: Enclosing): Unit =
    if (test) {
      logger.parameterList(level).markerMessageArgs(marker, format, args)
    }
}

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
```
