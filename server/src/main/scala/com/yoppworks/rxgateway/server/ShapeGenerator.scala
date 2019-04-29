package com.yoppworks.rxgateway.server

import com.yoppworks.rxgateway.api.{Color, Opacity, Shape, TetrisShape}

import scala.util.Random

object ShapeGenerator {
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

  def makeAColor: Color =
    Color(
      randomWithinRange(0, 255),
      randomWithinRange(0, 255),
      randomWithinRange(0, 255))

  def makeAnOpacity: Opacity =
    Opacity.values(randomWithinRange(0, Opacity.values.length - 1))

  def makeAShape: Shape =
    Shape(
      numberOfSides = randomWithinRange(MinNumberOfSides, MaxNumberOfSides),
      edgeColor = Some(makeAColor),
      fillColor = Some(makeAColor),
      opacity = makeAnOpacity,
      width = randomWithinRange(MinShapeWL, MaxShapeWL),
      height = randomWithinRange(MinShapeWL, MaxShapeWL),
      rotation = randomWithinRange(MinRotation, MaxRotation))

  def makeATetrisShape: TetrisShape =
    TetrisShape()

  private def randomWithinRange(min: Int, max: Int): Int =
    min + (new Random).nextInt((max - min) + 1)
}
