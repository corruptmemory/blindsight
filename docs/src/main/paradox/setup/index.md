# Setup

## SBT

```scala
val blindsightVersion = "0.1.0-SNAPSHOT"

resolvers += Resolver.bintrayRepo("tersesystems", "maven")
libraryDependencies += "com.tersesystems.blindsight" %% "blindsight" % blindsightVersion
libraryDependencies += "com.tersesystems.blindsight" %% "blindsight-logback" % blindsightVersion
```

You are also encouraged to install [Terse Logback](https://tersesystems.github.io/terse-logback/installation/#sbt).