package example.semantic

import com.tersesystems.blindsight.api._
import com.tersesystems.blindsight.logstash.Implicits._

trait UserEvent {
  def name: String
}

final case class UserLoggedInEvent(name: String, ipAddr: String) extends UserEvent

object UserLoggedInEvent {
  implicit val toStatement: ToStatement[UserLoggedInEvent] = ToStatement { instance =>
    Statement()
      .withMessage(instance.toString)
      .withArguments(Map("name" -> instance.name, "ipAddr" -> instance.ipAddr))
  }
}

final case class UserLoggedOutEvent(name: String, reason: String) extends UserEvent

object UserLoggedOutEvent {
  implicit val toStatement: ToStatement[UserLoggedOutEvent] = ToStatement { instance =>
    Statement()
      .withMessage(instance.toString)
      .withArguments(Map("name" -> instance.name, "reason" -> instance.reason))
  }
}
