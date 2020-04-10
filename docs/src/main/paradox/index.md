@@@ index

* [Setup](setup/index.md)
* [Usage](usage/index.md)
* [Extending](extending/index.md)

@@@

# Blindsight

Blindsight is a Scala logging API that allows for fluent logging, semantic logging, and context aware logging.  It is heavily informed by the [blog posts at tersesystems.com](https://tersesystems.com/category/logging/) and the [diagnostic logging showcase](https://github.com/tersesystems/terse-logback-showcase).

The name is taken from Peter Watts' excellent first contact novel, [Blindsight](https://en.wikipedia.org/wiki/Blindsight_\(Watts_novel\)).

## Principles

Blindsight has some organizing principles that inform the design.

* Loggers depend directly and solely on the SLF4J API.
* Loggers are instantiated through logger factories backed by service loaders.
* Easy access to the underlying SLF4J logger.
* Type class based operation, ideally without explicit imports.
* Extensible implementations.

Likewise, there are things that Blindsight eschews:

* No effects. 
* No "magic" implicit conversions.
* No constraints or configuration on SLF4J implementation.
* No FP library requirements; no need for scalaz, cats, zio etc.
* No formatting on the front end; messages should not contain JSON/XML.

## Overview

Blindsight breaks down logging statements into component parts through type classes:

* @ref:[Statement Classes](usage/statements.md) - `Markers`, `Message`, and `Arguments`, which compose to `Statement` through type class patterns.
* @ref:[Logstash Type Classes](usage/logstash.md) - Type class bindings of `Markers` and `Arguments` to [Logstash Markers and StructuredArguments](https://github.com/logstash/logstash-logback-encoder#event-specific-custom-fields).

Blindsight provides three different logging API: 

*  @ref:[slf4j](usage/slf4j.md) - an SLF4J compatible API, designed for drop in replacements, but with context markers.
*  @ref:[fluent](usage/fluent.md) - a [fluent builder API](https://martinfowler.com/bliki/FluentInterface.html) using provided type classes where logging statements are built progressively.
* @ref:[semantic](usage/semantic.md) - a [strongly typed](https://github.com/microsoft/perfview/blob/master/documentation/TraceEvent/TraceEventProgrammersGuide.md#introduction-strongly-typed-semantic-logging) API relying on user provided type classes that is well-suited for rich domain events.
