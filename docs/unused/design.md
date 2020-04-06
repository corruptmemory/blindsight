# Design


Blindsight does this by breaking down the components of logging.  In SLF4J, you have a logging API that looks like this: 

```java
public interface Logger {
  public boolean isInfoEnabled();
  public void info(String message);
}
```

In Blindsight, the API and the logging levels are all traits, and the `LoggerAPI` is a composition of those traits. 

```scala
trait LoggerComponent[P, M] {
  type Predicate <: P
  type Method    <: M
  type Self
}

trait InfoLoggerAPI[P, M] extends LoggerComponent[P, M] {
  def isInfoEnabled: Predicate
  def info: Method
}

trait LoggerAPI[P, M]
    extends LoggerComponent[P, M]
    with TraceLoggerAPI[P, M]
    with DebugLoggerAPI[P, M]
    with InfoLoggerAPI[P, M]
    with WarnLoggerAPI[P, M]
    with ErrorLoggerAPI[P, M]
```

Here's the SLF4J compatible API:

```scala
trait Logger extends LoggerAPI[LoggerPredicate, LoggerMethod]

trait LoggerPredicate {
  def apply(): Boolean
}

trait LoggerMethod {
  def apply(message: String): Unit
}
```

And here's the fluent API:

```scala
trait FluentLogger extends LoggerAPI[LoggerPredicate, FluentLoggerMethod]

trait FluentAPI {
  def marker[T: ToMarkers](instance: => T): FluentLoggerMethod.Builder
  def message[T: ToMessage](instance: => T): FluentLoggerMethod.Builder
  def argument[T: ToArguments](instance: => T): FluentLoggerMethod.Builder
  def cause(e: Throwable): FluentLoggerMethod.Builder
}

trait FluentLoggerMethod extends FluentAPI {
  def apply[T: ToStatement](instance: => T): Unit
}
```

Breaking down the API means that you can pass through only the `LoggerMethod`, or assemble your custom logging levels.  You have the option of extending `LoggerMethod` and `LoggerPredicate` with your own application specific logging API.   Blindsight is designed to work with you so that adding new functionality is easy.

For example, if you want to add entry and exit logging levels to the SLF4J compatible API with automatically applied markers, you can do this:

```scala
class EntryExitLogger(protected val logger: SLF4JLogger)
    extends ProxyLoggerAPI[LoggerPredicate, LoggerMethod] with Logger {
  override type Self = EntryExitLogger

  private val entryLogger: SLF4JLogger = logger.marker(MarkerFactory.getMarker("ENTRY"))
  private val exitLogger: SLF4JLogger = logger.marker(MarkerFactory.getMarker("EXIT"))

  def entry: Method = new SLF4JLoggerMethod(Level.TRACE, entryLogger)
  def exit: Method = new SLF4JLoggerMethod(Level.TRACE, exitLogger)
}
```
