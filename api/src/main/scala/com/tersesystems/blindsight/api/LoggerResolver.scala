package com.tersesystems.blindsight.api

import org.slf4j.Logger

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

  implicit val loggerToResolver: LoggerResolver[org.slf4j.Logger] = new LoggerResolver[org.slf4j.Logger] {
    override def resolveLogger(instance: org.slf4j.Logger): Logger = instance
  }
}

object LoggerResolver extends LowPriorityLoggerResolverImplicits
