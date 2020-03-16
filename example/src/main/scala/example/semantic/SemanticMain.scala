package example.semantic

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
    onlyLoggedInEventLogger.info(UserLoggedInEvent("mike", "10.0.0.1"))
  }
}
