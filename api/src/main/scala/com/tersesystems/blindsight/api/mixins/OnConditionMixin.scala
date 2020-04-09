package com.tersesystems.blindsight.api.mixins

trait OnConditionMixin {
  type Self
  def onCondition(test: => Boolean): Self
}
