package example.slf4j.conditional

import com.tersesystems.blindsight.slf4j._
import org.slf4j.Marker
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

class ConditionalLogger(test: => Boolean, protected val logger: Logger with ParameterListMixin)
    extends LoggerAPI[LoggerPredicate, ConditionalLoggerMethod] {
  override type Self      = ConditionalLogger
  override type Method    = ConditionalLoggerMethod
  override type Predicate = logger.Predicate

  def onCondition(test2: => Boolean): Self = new ConditionalLogger(test && test2, logger)

  override def isTraceEnabled: Predicate = logger.isTraceEnabled
  override def trace: Method             = new ConditionalLoggerMethod(Level.TRACE, test, logger)

  override def isDebugEnabled: Predicate = logger.isDebugEnabled
  override def debug: Method             = new ConditionalLoggerMethod(Level.DEBUG, test, logger)

  override def isInfoEnabled: Predicate = logger.isInfoEnabled
  override def info: Method             = new ConditionalLoggerMethod(Level.INFO, test, logger)

  override def isWarnEnabled: Predicate = logger.isWarnEnabled
  override def warn: Method             = new ConditionalLoggerMethod(Level.WARN, test, logger)

  override def isErrorEnabled: Predicate = logger.isErrorEnabled
  override def error: Method             = new ConditionalLoggerMethod(Level.ERROR, test, logger)
}

class ConditionalLoggerMethod(val level: Level,
                              test: => Boolean,
                              logger: Logger with ParameterListMixin)
    extends LoggerMethod {

  override def apply(message: String)(implicit line: Line, file: File, enclosing: Enclosing): Unit =
    if (test) {
      logger.parameterList(level).message(message)
    }

  override def apply(format: String,
                     arg: Any)(implicit line: Line, file: File, enclosing: Enclosing): Unit =
    if (test) {
      logger.parameterList(level).messageArg1(format, arg)
    }

  override def apply(format: String, arg1: Any, arg2: Any)(implicit line: Line,
                                                           file: File,
                                                           enclosing: Enclosing): Unit = if (test) {
    logger.parameterList(level).messageArg1Arg2(format, arg1, arg2)
  }

  override def apply(format: String,
                     args: Any*)(implicit line: Line, file: File, enclosing: Enclosing): Unit =
    if (test) {
      logger.parameterList(level).messageArgs(format, args)
    }

  override def apply(marker: Marker,
                     message: String)(implicit line: Line, file: File, enclosing: Enclosing): Unit =
    if (test) {
      logger.parameterList(level).markerMessage(marker, message)
    }

  override def apply(marker: Marker, format: String, arg: Any)(implicit line: Line,
                                                               file: File,
                                                               enclosing: Enclosing): Unit =
    if (test) {
      logger.parameterList(level).markerMessageArg1(marker, format, arg)
    }

  override def apply(marker: Marker, format: String, arg1: Any, arg2: Any)(
      implicit line: Line,
      file: File,
      enclosing: Enclosing): Unit = if (test) {
    logger.parameterList(level).markerMessageArg1Arg2(marker, format, arg1, arg2)
  }

  override def apply(marker: Marker, format: String, args: Any*)(implicit line: Line,
                                                                 file: File,
                                                                 enclosing: Enclosing): Unit =
    if (test) {
      logger.parameterList(level).markerMessageArgs(marker, format, args)
    }
}
