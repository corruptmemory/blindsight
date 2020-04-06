@@@ index

* [Setup](setup/index.md)
* [Usage](usage/index.md)
* [Extending](extending/index.md)

@@@

# Blindsight

Blindsight is a Scala logging API that allows for fluent logging, semantic logging, and context aware logging.

The name is taken from Peter Watts' excellent first contact novel, [Blindsight](https://en.wikipedia.org/wiki/Blindsight_\(Watts_novel\)).

## Requirements

Blindsight is based around SLF4J.  It does not configure or constrain SLF4J in any way, and is designed to defer layout decisions to the backend logging framework, so you can write the same logging event out to a text file, console, to a database, and to JSON on the backend.  

Having said that, the default assumption in the examples is that you are using [logstash-logback-encoder](https://github.com/logstash/logstash-logback-encoder) and [Terse Logback](https://tersesystems.github.io/terse-logback/) on the backend, and are roughly familiar with the [blog posts at tersesystems.com](https://tersesystems.com/category/logging/).

## Overview

Blindsight breaks down logging statements into component parts through type classes:

* `api` - `Markers`, `Message`, and `Arguments`, which compose to `Statement` through type class patterns.
* `logstash` - Type class bindings of `Markers` and `Arguments` to [Logstash Markers and StructuredArguments](https://github.com/logstash/logstash-logback-encoder#event-specific-custom-fields).

Blindsight provides three different logging API: 

*  @ref:[slf4j](usage/slf4j.md) - a straight reimplementation of the SLF4J API, designed for drop in replacements.
*  @ref:[fluent](usage/fluent.md) - a [fluent builder API](https://martinfowler.com/bliki/FluentInterface.html) using provided type classes where logging statements are built progressively.
* @ref:[semantic](usage/semantic.md) - a [strongly typed](https://github.com/microsoft/perfview/blob/master/documentation/TraceEvent/TraceEventProgrammersGuide.md#introduction-strongly-typed-semantic-logging) API relying on user provided type classes that is well-suited for rich domain events.
