# Logger Resolvers

```scala
trait LoggerResolver[T] {
  def resolveLogger(instance: T): org.slf4j.Logger
}
```

means you can do

```scala
implicit val requestToResolver: LoggerResolver[Request] = (instance: Request) => {
    ... // returns some org.slf4j.Logger instance
}
```

And from then on, you can do:

```scala
val myRequest: Request = ...
val logger = LoggerFactory.getLogger(myRequest)
```