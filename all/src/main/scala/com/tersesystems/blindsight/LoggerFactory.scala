package com.tersesystems.blindsight

import com.tersesystems.blindsight.api.{LoggerResolver, Markers}

trait LoggerFactory {
  def getLogger[T: LoggerResolver](instance: T): Logger
}

object LoggerFactory {

  import java.util.ServiceLoader

  private lazy val loggerFactory: LoggerFactory = {
    import javax.management.ServiceNotFoundException

    import scala.jdk.CollectionConverters._

    loggerFactoryLoader.iterator().asScala.find(_ != null).getOrElse {
      throw new ServiceNotFoundException("No logger factory found!")
    }
  }
  private val loggerFactoryLoader = ServiceLoader.load(classOf[LoggerFactory])

  def getLogger[T: LoggerResolver](instance: => T): Logger = {
    loggerFactory.getLogger(instance)
  }

  class Impl extends LoggerFactory {
    override def getLogger[T: LoggerResolver](instance: T): Logger = {
      val underlying = implicitly[LoggerResolver[T]].resolveLogger(instance)
      new Logger.Impl(new Logger.SLF4J(underlying, Markers.empty))
    }
  }

}
