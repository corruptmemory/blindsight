
## Fluent API

A [fluent builder interface](https://www.martinfowler.com/bliki/FluentInterface.html) is an API that relies heavily on method chaining to build up an expression.  The Blindsight fluent API works with `Markers`, `Message`, and `Arguments`, and uses type classes to map appropriately, using the `ToMarkers`, `ToMessage` and `ToArguments` type classes, respectively. 

The fluent API has an immediate advantage in that there's less overloading in the API, and there's more room to chain.  With type classes, it's possible to set up much richer [structured logging](https://tersesystems.com/blog/2020/03/10/a-taxonomy-of-logging/).   This is very useful when you are logging [events](https://www.honeycomb.io/blog/how-are-structured-logs-different-from-events/), because a complete picture is often not available at the beginning of the unit of work.

Blindsight provides a `logstash` typeclass mapping which maps between tuples, arrays, and JSON nodes to [Markers and StructuredArguments](https://github.com/logstash/logstash-logback-encoder#event-specific-custom-fields).

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
 