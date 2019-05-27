package com.yoppworks.rxgateway.utils

import scala.util.Random

trait RangeSelector {
  def randomWithinRange(min: Int, max: Int): Int =
    min + (new Random).nextInt((max - min) + 1)
}
