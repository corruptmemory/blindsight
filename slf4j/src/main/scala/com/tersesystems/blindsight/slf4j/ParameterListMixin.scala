package com.tersesystems.blindsight.slf4j

import org.slf4j.event.Level


trait ParameterListMixin {
  def parameterList(level: Level): ParameterList
}
