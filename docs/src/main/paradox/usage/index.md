@@@ index

* [SLF4J](slf4j.md)
* [Fluent](fluent.md)
* [Semantic](semantic.md)

@@@

## Overview

Blindsight is a series of wrappers on top of the SLF4J API: what Blindsight does do is breaks down the SLF4J into easily managable parts.  

### Statements

A single logging statement in SLF4J consists of a set of parameters in combination:

```java
Marker marker = ...
String message = ...
Object[] arguments = ...
logger.info(marker, message, arguments);
```

Each of these parameters are subject to aggregation: with a `marker.addReference(childMarker)`, or `message + moreText` or `join(arguments, extraArg)`.  Blindsight rationalizes these parameters into `Markers`, `Message`, and `Arguments`.

```scala
val markers = Markers(marker1, marker2)
val message = Message("some message")
val arguments = Arguments("arg1", 42, 1337)
```

You can use these with a standard `org.slf4j.Logger`, of course:

```scala
logger.info(markers.marker, message.withPlaceHolders(arguments), arguments.asArray: _*)
```

But Blindsight also provides a fluent API, which works with these natively: 

```scala
fluentLogger.info.marker(markers).message(message).argument(arguments).logWithPlaceholders()
```

## Type Classes

So far, so good.  But where it gets exciting is when you add [type classes patterns](https://www.theguardian.com/info/developer-blog/2016/dec/22/parental-advisory-implicit-content).  These are converters that take a given type, and convert it into something that Blindsight can use.

```scala
trait ToMarkers[T] {
  def toMarkers(instance: T): Markers
}
trait ToMessage[T] {
  def toMessage(instance: => T): Message
}
trait ToArguments[T] {
  def toArguments(instance: => T): Arguments
}
trait ToStatement[T] {
  def toStatement(instance: => T): Statement
}
```

You can set these up for yourself, but you don't have to.   Blindsight comes with bindings of `ToMarkers` and `ToArguments` to [Logstash Markers and StructuredArguments](https://github.com/logstash/logstash-logback-encoder#event-specific-custom-fields).  This lets you be far more expressive in constructing markers and arguments, and lets you pack far more information in.

For example, `LogstashMarkers` has the concept of a "key=value" pair that gets written out to JSON.  We can create a type class to represent that as a tuple:

```scala
trait LowPriorityMarkers {
  implicit val tupleStringToMarkers: ToMarkers[(String, String)] = ToMarkers {
    case (k, v) =>
      Markers(LogstashMarkers.append(k, v))
  }
}
object LowPriorityMarkers extends LowPriorityMarkers
```

And then we can do the following in the fluent API:

```scala
import LowPriorityMarkers._
logger.info.markers("userId" -> userId).log()
```

This is especially important for the semantic API. The semantic API is strongly typed, and so all conversion is handled through type classes:

```scala
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

val semanticLogger: SemanticLogger[UserLoggedOutEvent] = ...
logger.info(userLoggedOutEvent)
```

