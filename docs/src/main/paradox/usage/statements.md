# Statements

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
