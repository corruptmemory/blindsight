package com.tersesystems.blindsight.api

trait LoggerResolver[T] {
  def resolveLogger(instance: T): org.slf4j.Logger
}

trait LowPriorityLoggerResolverImplicits {
  implicit val stringToResolver: LoggerResolver[String] = new LoggerResolver[String] {
    override def resolveLogger(instance: String): org.slf4j.Logger = {
      val factory = org.slf4j.LoggerFactory.getILoggerFactory
      factory.getLogger(instance)
    }
  }

  implicit def classToResolver[T]: LoggerResolver[Class[T]] = new LoggerResolver[Class[T]] {
    override def resolveLogger(instance: Class[T]): org.slf4j.Logger = {
      val factory = org.slf4j.LoggerFactory.getILoggerFactory
      factory.getLogger(instance.getName)
    }
  }
}

object LoggerResolver extends LowPriorityLoggerResolverImplicits
