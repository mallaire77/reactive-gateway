package com.yoppworks.rxgateway.server

import com.yoppworks.rxgateway.api.{Color, Opacity, Shape, TetrisShape, TetrisShapeType}
import com.yoppworks.rxgateway.utils.{ChainingSyntax, RangeChooser}

trait TetrisShapeGenerator extends ChainingSyntax with RangeChooser {
  implicit class TetrisShape2DropSpots(tetrisShape: TetrisShape) {
    def withDropSpots(dropSpots: Seq[Int]): TetrisShape =
      dropSpots
        .isEmpty
        .pipe {
          case true =>
            tetrisShape.withDropSpot(0)

          case false =>
            tetrisShape.withDropSpot(dropSpots(randomWithinRange(0, dropSpots.length - 1)))
        }
  }

  private val LShape =
    Shape(numberOfSides = 4, height = 75, width = 50)

  private val LTetrisShape =
    TetrisShape(shape = Some(LShape), shapeType = TetrisShapeType.LShape, animate = true)

  val LMirrorTetrisShape =
    TetrisShape(shape = Some(LShape), shapeType = TetrisShapeType.LMirrorShape, animate = true)

  private val TShape =
    Shape(numberOfSides = 4, height = 50, width = 75)

  private val TTetrisShape =
    TetrisShape(shape = Some(TShape), shapeType = TetrisShapeType.TShape, animate = true)

  private val IShape =
    Shape(numberOfSides = 4, height = 100, width = 25)

  private val ITetrisShape =
    TetrisShape(shape = Some(IShape), shapeType = TetrisShapeType.IShape, animate = true)

  private val ZShape =
    Shape(numberOfSides = 4, height = 50, width = 75)

  private val ZTetrisShape =
    TetrisShape(shape = Some(ZShape), shapeType = TetrisShapeType.ZShape, animate = true)

  private val ZMirrorTetrisShape =
    TetrisShape(shape = Some(ZShape), shapeType = TetrisShapeType.ZMirrorShape, animate = true)

  private val AShape =
    Shape(numberOfSides = 4, height = 50, width = 50)

  private val ATetrisShape =
    TetrisShape(shape = Some(AShape), shapeType = TetrisShapeType.AShape, animate = true)

  private val All: Seq[TetrisShape] =
    Seq(
      LTetrisShape,
      LMirrorTetrisShape,
      TTetrisShape,
      ITetrisShape,
      ZTetrisShape,
      ZMirrorTetrisShape,
      ATetrisShape
    )

  private val TetrisRotations =
    Seq(0, 90, 180, 270, 360)

  def makeATetrisRotation: Int =
    TetrisRotations(randomWithinRange(0, TetrisRotations.length - 1))

  def makeAColor: Color =
    Color(
      randomWithinRange(0, 255),
      randomWithinRange(0, 255),
      randomWithinRange(0, 255))

  def makeAnOpacity: Opacity =
    Opacity.values(randomWithinRange(0, Opacity.values.length - 1))

  def makeATetrisShape: TetrisShape =
    randomize(All(randomWithinRange(0, All.length - 1)))

  private def randomize(tetrisShape: TetrisShape): TetrisShape =
    tetrisShape
      .withShape(
        tetrisShape
          .shape
          .map(pretty)
          .map(adjustRotation(makeATetrisRotation))
          .orNull
      )

  private def pretty: Shape => Shape =
    _.withEdgeColor(makeAColor).withFillColor(makeAColor).withOpacity(makeAnOpacity)

  private def adjustRotation(rotation: Int)(shape: Shape): Shape =
    rotation match {
      case x if x == 90 || x == 270 =>
        shape.withHeight(shape.width).withWidth(shape.height).withRotation(x)

      case x =>
        shape.withRotation(x)
    }
}

object TetrisShapeGenerator extends TetrisShapeGenerator
