package com.yoppworks.rxgateway.server

import com.yoppworks.rxgateway.api.{Shape, TetrisShape}
class ShapeGenerator {
  def makeAShape: Shape = {
    Shape()
  }
  def makeATetrisShape: TetrisShape = {
    TetrisShape()
  }
}
