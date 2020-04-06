# Blindsight

Blindsight breaks down logging statements into component parts through type classes:

* `api` - `Markers`, `Message`, and `Arguments`, which compose to `Statement` through type class patterns.
* `logstash` - Type class bindings of `Markers` and `Arguments` to [Logstash Markers and StructuredArguments](https://github.com/logstash/logstash-logback-encoder#event-specific-custom-fields).

Blindsight provides three different logging API: 

* `slf4j` - a straight reimplementation of the SLF4J API, designed for drop in replacements.
* `fluent` - a fluent builder API using provided type classes where logging statements are built progressively.
* `semantic` - a strongly typed API relying on user provided type classes that is well-suited for rich domain events.
  
Blindsight is designed to defer layout decisions to the backend logging framework, so you can write the same logging event out to a text file, console, to a database, and to JSON on the backend.  Having said that, you are strongly encouraged to use [logstash-logback-encoder](https://github.com/logstash/logstash-logback-encoder) and [Terse Logback](https://tersesystems.github.io/terse-logback/).
