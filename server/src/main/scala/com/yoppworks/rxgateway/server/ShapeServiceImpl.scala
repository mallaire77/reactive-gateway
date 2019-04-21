package com.yoppworks.rxgateway.server

import scala.concurrent.{ExecutionContext, Future}

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.yoppworks.rxgateway.api._

/** Unit Tests For ShapeServiceImpl */
case class ShapeServiceImpl()(implicit executor: ExecutionContext) extends
  ShapeService {
  
  def prepareShapes(in: PrepareShapes): Future[ShapeServiceResult] = Future {
    ShapeServiceResult()
  }
  
  def getAShape(in: GetAShape): Future[ShapeServiceResult] = Future {
    ShapeServiceResult()
  }
  
  def getSomeShapes(in: GetSomeShapes): Source[Shape, NotUsed] = {
    Source.empty[Shape]
  }
  
  def getSomeTetrisShapes(in: GetSomeTetrisShapes)
  : Source[TetrisShape, NotUsed] = {
    Source.empty[TetrisShape]
  }
  
  def releaseShapes(
    in: com.yoppworks.rxgateway.api.ReleaseShapes
  ): Future[com.yoppworks.rxgateway.api.ShapeServiceResult] = Future {
    ShapeServiceResult()
  }
}
