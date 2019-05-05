package com.yoppworks.rxgateway.server

import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Keep, Source}
import akka.grpc.scaladsl.Metadata
import com.yoppworks.rxgateway.api._
import com.yoppworks.rxgateway.server.EnforcedProtocol._

/** Unit Tests For ShapeServiceImpl */
case class ShapeServiceImpl() extends ShapeService {
  
  final val headerKey = "X-USERNAME"
  
  private def checkTransition(
    metadata: Metadata, transition: ProtocolStateTransition
  )(
    doit: () ⇒ Unit
  ): Future[ShapeServiceResult] = {
    val id = metadata
      .getText(headerKey)
      .orElse(Some(UUID.randomUUID().toString)).get
    tryTransition(id,transition).map {
      case None ⇒
        doit()
        ShapeServiceResult(viable = true)
      case Some(message) ⇒
        ShapeServiceResult(viable = false, message)
    }
  }
  
  def prepareShapes(in: PrepareShapes, metadata: Metadata)
  : Future[ShapeServiceResult] = {
    checkTransition(metadata, ToPrepareShapes) { () ⇒
      // Implement prepareShapes here
    }
  }
  
  def prepareShapes(in: PrepareShapes): Future[ShapeServiceResult] =
    Future {
      ShapeServiceResult()
    }
  
  def getAShape(in: PrepareShapes, metadata: Metadata)
  : Future[ShapeServiceResult] = {
    checkTransition(metadata, ToGetAShape) { () ⇒
      // Implement getAShape here
    }
  }
  
  def getAShape(in: GetAShape): Future[ShapeServiceResult] =
    Future {
      ShapeServiceResult()
    }
  
  def getSomeShapes(in: GetSomeShapes): Source[Shape, NotUsed] =
    Source
      .tick(0.seconds, in.intervalMs.milliseconds, ShapeGenerator.makeAShape)
      .zipWithIndex
      .takeWhile {
        case (_, idx) =>
          idx < in.numberOfShapes
      }
      .map {
        case (shape, _) =>
          shape
      }
      .viaMat(Flow[Shape].map(identity))(Keep.right)
  
  def getSomeTetrisShapes(in: GetSomeTetrisShapes): Source[TetrisShape, NotUsed] = {
    Source.empty[TetrisShape]
  }
  
  def releaseShapes(
    metadata: Metadata,
    in: ReleaseShapes
  ): Future[ShapeServiceResult] =
    checkTransition(metadata, ToReleaseShapes) { () ⇒
      // Implement releaseShapes here
    }
  
  def releaseShapes(
    in: ReleaseShapes
  ): Future[ShapeServiceResult] =
    Future {
      ShapeServiceResult()
    }

}
