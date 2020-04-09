@@@ index

* [Statements](statements.md)
* [Logstash Mappings](logstash.md)
* [Resolvers](resolvers.md)
* [SLF4J API](slf4j.md)
* [Fluent API](fluent.md)
* [Semantic API](semantic.md)

@@@

# Usage

The simplest possible logger is:

```scala
import com.tersesystems.blindsight.LoggerFactory
val logger = LoggerFactory.getLogger(getClass)
logger.info("hello world")
```

@@toc { depth=2 }