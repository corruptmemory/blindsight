# Flow Logger

Here's a logger extended with entry and exit methods:

```scala
import com.tersesystems.blindsight._
import com.tersesystems.blindsight.slf4j._
import org.slf4j.MarkerFactory
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

class FlowLogger(protected val logger: Logger)
    extends LoggerAPI.Proxy[LoggerPredicate, LoggerMethod] with Logger {
  override type Parent = Logger
  override type Self = FlowLogger

  private val entryLogger = logger.marker(MarkerFactory.getMarker("ENTRY"))
  private val exitLogger = logger.marker(MarkerFactory.getMarker("EXIT"))

  def entry: LoggerFlowMethod = {
    new LoggerFlowMethod {
      override def apply[B](message: String)(block: ExitHandle => B)(implicit enclosing: Enclosing): B = {
        entryLogger.trace(s"${enclosing.value} enter: $message")
        block(new ExitHandle {
          override def apply[T: ToStatement](instance: => T): T = {
            val result = instance
            val s = implicitly[ToStatement[T]].toStatement(result)
            exitLogger.trace(s"${enclosing.value} exit: result = ${s.message.toString}", s.arguments.asArray: _*)
            result
          }
        })
      }
    }
  }

  override def sourceInfoMarker(level: Level,
                                line: Line,
                                file: File,
                                enclosing: Enclosing): Markers = Markers.empty

  override def marker[T: ToMarkers](markerInstance: T) = new FlowLogger(logger.marker(markerInstance))

  override def markerState: Markers = logger.markerState
}

/**
 * This trait is an extension on the logger method that allows for
 * flow control using entry / exit handles with logging.
 */
trait LoggerFlowMethod {

  trait ExitHandle {
    def apply[T: ToStatement](instance: => T): T
  }

  def apply[B](message: String)(block: ExitHandle => B)(implicit enclosing: Enclosing): B
}

object FlowMain {

  def main(args: Array[String]): Unit = {
    val underlying = org.slf4j.LoggerFactory.getLogger(getClass)
    val logger = new FlowLogger(new SLF4JLogger(underlying, Markers.empty))

    logger.info("About to execute flow")

    // Need to specify to statement type class for the exit type:
    implicit val toIntStatement: ToStatement[Int] = ToStatement(i => Statement().withMessage(i.toString))
    val result: Int = logger.entry("entering") { exit =>
      exit(1 + 2)
    }
  }
}
```

In flat file format:

```
FgEdhil1mYM6O0Qbm7EAAA 2020-04-05T23:13:38.958+0000 [INFO ] example.flow.FlowMain$ in main  - About to execute flow
FgEdhil1mdQ6O0Qbm7EAAA 2020-04-05T23:13:39.039+0000 [TRACE] example.flow.FlowMain$ in main Set(ENTRY) [ ENTRY ] - example.flow.FlowMain.main result enter: entering
FgEdhil1md06O0Qbm7EAAA 2020-04-05T23:13:39.048+0000 [TRACE] example.flow.FlowMain$ in main Set(EXIT) [ EXIT ] - example.flow.FlowMain.main result exit: result = 3
```

JSON:

```json
{"id":"FgEdhil1mYM6O0Qbm7EAAA","relative_ns":-285601,"tse_ms":1586128418958,"start_ms":null,"@timestamp":"2020-04-05T23:13:38.958Z","@version":"1","message":"About to execute flow","logger_name":"example.flow.FlowMain$","thread_name":"main","level":"INFO","level_value":20000}
{"id":"FgEdhil1mdQ6O0Qbm7EAAA","relative_ns":80095000,"tse_ms":1586128419039,"start_ms":null,"@timestamp":"2020-04-05T23:13:39.039Z","@version":"1","message":"example.flow.FlowMain.main result enter: entering","logger_name":"example.flow.FlowMain$","thread_name":"main","level":"TRACE","level_value":5000,"tags":["Set(ENTRY)","ENTRY"]}
{"id":"FgEdhil1md06O0Qbm7EAAA","relative_ns":88443600,"tse_ms":1586128419048,"start_ms":null,"@timestamp":"2020-04-05T23:13:39.048Z","@version":"1","message":"example.flow.FlowMain.main result exit: result = 3","logger_name":"example.flow.FlowMain$","thread_name":"main","level":"TRACE","level_value":5000,"tags":["Set(EXIT)","EXIT"]}
```
