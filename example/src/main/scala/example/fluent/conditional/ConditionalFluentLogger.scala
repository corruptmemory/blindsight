package example.fluent.conditional

import com.tersesystems.blindsight.ToStatement
import com.tersesystems.blindsight.fluent.{SLF4JFluentLogger, SLF4JFluentLoggerMethod}
import com.tersesystems.blindsight.slf4j.{LoggerAPI, LoggerPredicate}
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

class ConditionalFluentLogger(test: => Boolean, logger: SLF4JFluentLogger) extends LoggerAPI[LoggerPredicate, ConditionalFluentLoggerMethod] {
  override type Predicate = LoggerPredicate
  override type Method = ConditionalFluentLoggerMethod
  override type Self = ConditionalFluentLogger

  def onCondition(test2: Boolean): Self = new ConditionalFluentLogger(test && test2, logger)

  override def isTraceEnabled: Predicate = logger.isTraceEnabled
  override def trace: Method = new ConditionalFluentLoggerMethod(Level.TRACE, test, logger)

  override def isDebugEnabled: Predicate = logger.isDebugEnabled
  override def debug: Method = new ConditionalFluentLoggerMethod(Level.DEBUG, test, logger)

  override def isInfoEnabled: Predicate = logger.isInfoEnabled
  override def info: Method = new ConditionalFluentLoggerMethod(Level.INFO, test, logger)

  override def isWarnEnabled: Predicate = logger.isWarnEnabled
  override def warn: Method = new ConditionalFluentLoggerMethod(Level.WARN, test, logger)

  override def isErrorEnabled: Predicate = logger.isErrorEnabled
  override def error: Method = new ConditionalFluentLoggerMethod(Level.ERROR, test, logger)
}

class ConditionalFluentLoggerMethod(level: Level, test: => Boolean, logger: SLF4JFluentLogger)
  extends SLF4JFluentLoggerMethod(level, logger) {

  override def apply[T: ToStatement](instance: => T)(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
    if (test) {
      val statement = implicitly[ToStatement[T]].toStatement(instance)
      logger.parameterList(level).executeStatement(statement)
    }
  }
}