package com.yoppworks.rxgateway.server

import com.yoppworks.rxgateway.api.{Color, Opacity}
import com.yoppworks.rxgateway.utils.RangeSelector

trait ShapeGenerator extends RangeSelector {
  def makeAColor: Color =
    Color(
      randomWithinRange(0, 255),
      randomWithinRange(0, 255),
      randomWithinRange(0, 255)
    )

  def makeAnOpacity: Opacity =
    Opacity.values(randomWithinRange(0, Opacity.values.length - 1))
}
