package com.tersesystems.blindsight

import com.tersesystems.blindsight.api._
import com.tersesystems.blindsight.fluent.FluentLogger
import com.tersesystems.blindsight.semantic.SemanticLogger
import com.tersesystems.blindsight.slf4j.SLF4JLogger

trait Logger extends SLF4JLogger {
  def underlying: org.slf4j.Logger
  def fluent: FluentLogger
  def refine[MessageType]: SemanticLogger[MessageType]
}

trait LoggerFactory {
  def getLogger[T : LoggerResolver](instance: T): Logger
}

object LoggerFactory {
  import java.util.ServiceLoader
  private val loggerFactoryLoader = ServiceLoader.load(classOf[LoggerFactory])

  private lazy val loggerFactory: LoggerFactory = {
    import scala.collection.JavaConverters._
    import javax.management.ServiceNotFoundException

    loggerFactoryLoader.iterator().asScala.find(_ != null).getOrElse {
      throw new ServiceNotFoundException("No logger factory found!")
    }
  }

  def getLogger[T : LoggerResolver](instance: => T): Logger = {
    loggerFactory.getLogger(instance)
  }
}
