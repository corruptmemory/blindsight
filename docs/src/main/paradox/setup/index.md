# Setup

To install Blindsight, add the following lines to the project settings in SBT:

```
val blindsightVersion = "<latest-version>"

resolvers += Resolver.bintrayRepo("tersesystems", "maven")
libraryDependencies += "com.tersesystems.blindsight" %% "blindsight" % blindsightVersion
```

Blindsight depends on SLF4J using a service loader pattern, which is typically [Logback](http://logback.qos.ch/) or [Log4J 2](https://logging.apache.org/log4j/2.x/).  This means you should also plug in one of the service loader implementations, provided below.

## Logback

The recommended option for Logback is to use `blindsight-logstash`, which includes source information (line, file, enclosing) as logstash markers on the logger and provides a mapping for `Arguments` and `Markers`. 
 
```
libraryDependencies += "net.logstash.logback" % "blindsight-logstash" % blindsightVersion
```

If you are not using `logstash-logback-encoder`, you should use `blindsight-logback`, which has a library dependency on Logback but does not depend on Logstash and does not include source code information.

```
libraryDependencies += "com.tersesystems.blindsight" %% "blindsight-logback" % blindsightVersion
``` 

It is recommended (but not required) to use [Terse Logback](https://tersesystems.github.io/terse-logback/) on the backend:

```
# resolvers += Resolver.bintrayRepo("tersesystems", "maven")
libraryDependencies += "com.tersesystems.logback" % "logback-structured-config" % "0.16.0"
```

## Log4J 2

To install the Log4J 2 implementation of Blindsight, add the following lines:

```dtd
libraryDependencies += "com.tersesystems.blindsight" %% "blindsight-log4j2" % blindsightVersion
```

This library depends on `log4j-slf4j-impl`, binding the Log4J 2 to the SLF4J API.  It does not include source code information.