
## Semantic API

A semantic logging API is [strongly typed](https://github.com/microsoft/perfview/blob/master/documentation/TraceEvent/TraceEventProgrammersGuide.md) and does not have the same construction oriented approach as the fluent API.  Instead, the type of the instance is presumed to have a mapping directly to the attributes being logged.

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