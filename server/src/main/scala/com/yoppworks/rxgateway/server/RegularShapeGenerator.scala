package com.yoppworks.rxgateway.server

import com.yoppworks.rxgateway.api.Shape

trait RegularShapeGenerator extends ShapeGenerator {
  private val MinNumberOfSides =
    3

  private val MaxNumberOfSides =
    10

  private val MinShapeWL =
    10//px

  private val MaxShapeWL =
    250//px

  private val MinRotation =
    0

  private val MaxRotation =
    360

  def makeAShape: Shape =
    Shape(
      numberOfSides = randomWithinRange(MinNumberOfSides, MaxNumberOfSides),
      edgeColor = Some(makeAColor),
      fillColor = Some(makeAColor),
      opacity = makeAnOpacity,
      width = randomWithinRange(MinShapeWL, MaxShapeWL),
      height = randomWithinRange(MinShapeWL, MaxShapeWL),
      rotation = randomWithinRange(MinRotation, MaxRotation).toFloat
    )
}

object RegularShapeGenerator extends RegularShapeGenerator
