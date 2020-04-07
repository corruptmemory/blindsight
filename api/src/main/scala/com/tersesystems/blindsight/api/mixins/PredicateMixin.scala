package com.tersesystems.blindsight.api.mixins

import org.slf4j.event.Level

trait PredicateMixin[Predicate] {
  def predicate(level: Level): Predicate
}
