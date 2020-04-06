
## SLF4J API

The SLF4J API has the same logger methods and type signature as SLF4J, but has a couple of extra features in the `Logger` interface itself.

Notably, you can pass in something that is not a marker, and provided you have a `ToMarker` in implicit scope, you can get it auto-converted.

Markers can also be passed into the logger through the `logger.marker` method.  This provides a logger of the same time that will automatically apply the marker on any logging call.  This is extremely useful when you want to build up state inside a logger without having to expose it explicitly.  Under this API, you pass in a logger, and any markers you explicitly add are merged on top of the logger's markers.

Finally, you can also use the `LoggerAPI` traits to selectively limit the abilities of the logger.  For example, you can create a logger which only logs at info level. 

```scala
import com.tersesystems.blindsight._
import com.tersesystems.blindsight.slf4j.{InfoLoggerAPI, Logger}
import org.slf4j.MarkerFactory

object Slf4jMain {

  final case class FeatureFlag(flagName: String)

  object FeatureFlag {
    implicit val toMarkers: ToMarkers[FeatureFlag] = ToMarkers { instance =>
      Markers(MarkerFactory.getDetachedMarker(instance.flagName))
    }
  }

  def main(args: Array[String]): Unit = {
    val underlying = org.slf4j.LoggerFactory.getLogger(getClass)
    val logger: Logger = Logger(underlying)

    val featureFlag = FeatureFlag("flag.enabled")
    // this is not a marker, but is converted via type class.
    if (logger.isDebugEnabled(featureFlag)) {
      logger.debug("this is a test")
    }

    logger.info("hello world")

    val loggerWithFeatureFlagMarker = logger.marker(featureFlag)
    loggerWithFeatureFlagMarker.info("I have the feature flag marker automatically added!")

    val onlyInfo = new InfoLoggerAPI[loggerWithFeatureFlagMarker.Predicate, loggerWithFeatureFlagMarker.Method] {
      override type Self = loggerWithFeatureFlagMarker.Self
      override type Predicate = loggerWithFeatureFlagMarker.Predicate
      override type Method = loggerWithFeatureFlagMarker.Method

      override def isInfoEnabled: Predicate = loggerWithFeatureFlagMarker.isInfoEnabled
      override def info: Method = loggerWithFeatureFlagMarker.info
    }
    onlyInfo.info("this logger can only log info methods")
  }
}
```

produces the following in `application.log`:

```
FgEdh9F8h8EHR2iDc3YgAA 2020-04-05T18:27:08.435+0000 [DEBUG] example.slf4j.Slf4jMain$ in main  - this is a test
FgEdh9F8h8GnR2iDc3YgAA 2020-04-05T18:27:08.440+0000 [INFO ] example.slf4j.Slf4jMain$ in main  - hello world
FgEdh9F8h8IHR2iDc3YgAA 2020-04-05T18:27:08.443+0000 [INFO ] example.slf4j.Slf4jMain$ in main Set(M1, M2) [ M1, M2 ] - I should have two markers
FgEdh9F8h8InR2iDc3YgAA 2020-04-05T18:27:08.444+0000 [INFO ] example.slf4j.Slf4jMain$ in main Set(M1, M2) [ M1, M2 ] - good
```

and the following JSON:

```json
{"id":"FgEdh9F8h8EHR2iDc3YgAA","relative_ns":-365200,"tse_ms":1586111228435,"start_ms":null,"@timestamp":"2020-04-05T18:27:08.435Z","@version":"1","message":"this is a test","logger_name":"example.slf4j.Slf4jMain$","thread_name":"main","level":"DEBUG","level_value":10000}
{"id":"FgEdh9F8h8GnR2iDc3YgAA","relative_ns":2997600,"tse_ms":1586111228440,"start_ms":null,"@timestamp":"2020-04-05T18:27:08.440Z","@version":"1","message":"hello world","logger_name":"example.slf4j.Slf4jMain$","thread_name":"main","level":"INFO","level_value":20000}
{"id":"FgEdh9F8h8IHR2iDc3YgAA","relative_ns":6442600,"tse_ms":1586111228443,"start_ms":null,"@timestamp":"2020-04-05T18:27:08.443Z","@version":"1","message":"I should have two markers","logger_name":"example.slf4j.Slf4jMain$","thread_name":"main","level":"INFO","level_value":20000,"tags":["Set(M1, M2)","M1","M2"]}
{"id":"FgEdh9F8h8InR2iDc3YgAA","relative_ns":7005600,"tse_ms":1586111228444,"start_ms":null,"@timestamp":"2020-04-05T18:27:08.444Z","@version":"1","message":"good","logger_name":"example.slf4j.Slf4jMain$","thread_name":"main","level":"INFO","level_value":20000,"tags":["Set(M1, M2)","M1","M2"]}
```