# Blindsight

Blindsight is a Scala logging API focused on providing extensible, domain specific functionality for logging, using type classes and Scala's type hierarchy to provide transparent logging.  Blindsight does this while providing a 100% SLF4J compatible point-for-point API, so you can immediately swap out SLF4J for Blindsight with no other changes.
  
Blindsight does this by breaking down the components of logging.  In SLF4J, you have a logging API that looks like this: 

```java
public interface Logger {
  public boolean isInfoEnabled();
  public void info(String message);
}
```

In Blindsight, the API consists of `LoggerMethod` and `LoggerPredicate` which work as follows:

```scala
trait LoggerPredicate {
  def apply(): Boolean
}

trait LoggerMethod { 
  def apply(message: String): Unit
}

trait InfoLogger { 
  def isInfoEnabled: LoggerPredicate
  def info: LoggerMethod
}
```

You have the option of extending `LoggerMethod` and `LoggerPredicate` with your own application specific logging API.   Blindsight is designed to work with you so that adding new functionality is easy.

This doesn't mean much by itself, so let's show an example.  This is Blindsight:

```scala
logger.info("hello world!")
````




Blindsight provides options for strongly-typed (aka "semantic") event logging through the EventLogger API.

```scala
val userEventLogger: SLF4JEventLogger[UserEvent] = ...
userEventLogger.info(UserLoggedInEvent("steve", InetAddress.getLocalHost.toString))
```

## Logging

### Line Numbers

 common "source code" context to your program at runtime.   https://github.com/lihaoyi/sourcecode#logging 

```scala
trait SourceInfoSupport {
  def sourceInfoMarker(
      level: Level,
      line: Line,
      file: File,
      enclosing: Enclosing
  ): Option[Marker]
}
```
 
```scala
trait LogstashSourceInfoSupport extends SourceInfoSupport {
  override def sourceInfoMarker(
      level: Level,
      line: Line,
      file: File,
      enclosing: Enclosing
  ): Option[Marker] = {
    import com.tersesystems.blindsight.Implicits._
    val lineMarker      = Markers.append("line", line.value)
    val fileMarker      = Markers.append("file", file.value)
    val enclosingMarker = Markers.append("enclosing", enclosing.value)
    Some(lineMarker :+ fileMarker :+ enclosingMarker)
  }
}
``` 

### Extensible API

We can extend the logging API.  Here's how you do it:

```scala
trait FancyExtendedAPI {
  def cause(e: Throwable): MyFancyLoggerMethod.Builder
  def message(msg: String): MyFancyLoggerMethod.Builder
  def errorCode(errorCode: ErrorCode): MyFancyLoggerMethod.Builder
}

trait MyFancyLoggerMethod extends LoggerMethod with FancyExtendedAPI

trait MyFancyLogger extends Logger[LoggerPredicate, MyFancyLoggerMethod] {
  override type Method   = MyFancyLoggerMethod
}
```

And then delegate through the original `LoggerMethod` implementation, allowing the builder to have an extra `log` method:

```scala
class MyFancyLoggerMethodImpl(method: LoggerMethod) extends MyFancyLoggerMethod {
  override def level: Level = method.level

  override def apply[T: ContravariantStatement](
      instance: T
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
    method.apply(instance)
  }

  final case class BuilderImpl(
      m: Option[String] = None,
      code: Option[ErrorCode] = None,
      e: Option[Throwable] = None
  ) extends MyFancyLoggerMethod.Builder {

    override def cause(e: Throwable): MyFancyLoggerMethod.Builder  = copy(e = Some(e))
    override def message(msg: String): MyFancyLoggerMethod.Builder = copy(m = Some(msg))
    override def errorCode(errorCode: ErrorCode): MyFancyLoggerMethod.Builder =
      copy(code = Some(errorCode))

    override def log(): Unit = {
      // ...case statement depending on what's valued
    }
  }

  override def cause(e: Throwable): MyFancyLoggerMethod.Builder  = BuilderImpl(e = Some(e))
  override def message(msg: String): MyFancyLoggerMethod.Builder = BuilderImpl(m = Some(msg))
  override def errorCode(errorCode: ErrorCode): MyFancyLoggerMethod.Builder =
    BuilderImpl(code = Some(errorCode))
}

object MyFancyLoggerMethod {
  trait Builder extends FancyExtendedAPI {
    def log(): Unit
  }
}
```

### Building up context with Markers

- Markers are not immutable, safe to merge across multiple?

https://www.slideshare.net/Takipi/advanced-production-debugging

### Conditional Support



### Strongly Typed Logging

Why strongly typed (semantic) logging is important.
https://github.com/microsoft/perfview/blob/master/documentation/TraceEvent/TraceEventProgrammersGuide.md

Strongly typed logging is mostly done when you have operational logging and want to specifically indicate an event or failure. 
 
You can create a logger that accepts events only of a certain type:

```scala
val userEventLogger: SLF4JEventLogger[UserEvent] = ...
userEventLogger.info(UserLoggedInEvent("steve", InetAddress.getLocalHost.toString))
```

In the case where there isn't an exact match, implicit resolution will match on the parent trait:

```scala
import com.tersesystems.blindsight._

trait UserEvent {
  def name: String
}

object UserEvent {
  implicit val personToArguments: ToStatement[UserEvent] = ToStatement { instance =>
    Statement.markerString {
      val marker = MarkerFactory.getDetachedMarker("UNKNOWN_USER_EVENT")
      (marker, instance.name)
    }
  }
}
```

The implicit type class is `ToStatement`, and the statement must be one of the arguments of `Statement`, which correspond to the SLF4J API: for example, the `(Marker, String)` tuple maps to a call of `logger.info(marker, msg)`.

You can always specify additional data for the specific event:

```scala
final case class UserLoggedInEvent(name: String, ipAddr: String) extends UserEvent

object UserLoggedInEvent {
  implicit val personWithAddressToArguments: ToStatement[UserLoggedInEvent] = ToStatement {
    instance =>
      Statement.markerString {
        val marker = MarkerFactory.getDetachedMarker("USER_LOGGED_IN")
        (marker, instance.name + " ipAddr " + instance.ipAddr)
      }
  }
}

final case class UserLoggedOutEvent(name: String, reason: String) extends UserEvent
object UserLoggedOutEvent {
  implicit val personWithAgeToArguments: ToStatement[UserLoggedOutEvent] = ToStatement { instance =>
    Statement.markerString {
      val marker = MarkerFactory.getDetachedMarker("USER_LOGGED_OUT")
      (marker, instance.name + " reason " + instance.reason)
    }
  }
}
```

Strongly typed logging is often used in conjunction with structured logging.

Use logstash-logback-encoder and terse-logback. :-)

https://github.com/SemanticRecord/talaan

https://www.kartar.net/2015/12/structured-logging/

### Flow Control / Enter & Exit Logging

### Causality / Tree Logging

#### Eliot

https://eliot.readthedocs.io/en/stable/quickstart.html#adding-eliot-logging

https://eliot.readthedocs.io/en/stable/generating/actions.html

#### Logtree

- Logtree?

https://github.com/lancewalton/treelog

Need an example here.

## Metrics

- Handling metrics through schema?

Do it on the backend.  Handle events through means of several metrics appender.  When you post an event, there's a metrics appender than handles the aggregation.  This is actually much better than handling metrics inline with the code, because there are locks around histograms etc.  This makes it async and offline from the processing thread, and lets you replace your metrics code later.

## Tracing

https://users.scala-lang.org/t/overriding-implicit-contexts/4696/2

https://crates.io/crates/tracing

http://smallcultfollowing.com/babysteps/blog/2020/02/11/async-interview-6-eliza-weisman/

https://github.com/open-telemetry/opentelemetry-java/blob/master/QUICKSTART.md#create-basic-span

TODO Work with tracing API?
     https://tracing.rs/tracing/
     https://docs.honeycomb.io/getting-data-in/java/beeline/

## Context Resolution

Ties in to operation / "unit of work" activities.

### Through Scoping

You're in an object that has a context already, and can reference it directly.

Either you're an inner class, or it's provided as a parameter, or there's only one.

### Through Implicits

Doesn't compile if you don't have implicit in scope
Requires modification of signatures

Works great if you own the code

- TODO Use MDC as context if nothing else is around?  Leverage context as much as possible?
-

### Through Thread Local Storage

Works great if you're always using the same thread.

### Instrumentation

Works great if you have byte code instrumentation for the code base.

logback-bytebuddy.

### Through Lookup

Tie the logger / context to the unit of work / operation id.

Then use a resolver with that operation id to find the best context.

Something in scope?  Use it.  Something in thread-local?  Use that.  If not, pull it directly from lookup.

Downside -- anything can access the context and log with it, given the id.
Also have to cache or explicitly remove context.

Also requires that you have a unique id you can look up for everything, and it's fast enough to do so.

FP heavy code can log perfectly well in this scenario, because all you need is the tag and then you can look up from wherever.  It's the resolver's job to find something that can match it.

Ability to deal with FP heavy code (factories for functions?)  Covering exceptional cases and failures.
