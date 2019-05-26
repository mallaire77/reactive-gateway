package com.yoppworks.rxgateway.server

import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Keep, Source}
import akka.grpc.scaladsl.Metadata

import com.yoppworks.rxgateway.api._
import com.yoppworks.rxgateway.server.ShapeEnforcedProtocol._
import com.yoppworks.rxgateway.utils.ChainingSyntax

import scala.concurrent.Future
import scala.concurrent.duration._

case class ShapeServiceImpl() extends ShapeServicePowerApi with ChainingSyntax {
  private type Message = String

  private final val HeaderKey = "X-USERNAME"

  private final val SuccessfulShapeServiceResult = "Success"

  private final val FailedResult: Message => Result =
    Result(viable = false, _)

  private final val FailedShapeResult: Message => ShapeResult =
    ShapeResult(viable = false, _)

  private final val FailedTetrisShapeResult: Message => TetrisShapeResult =
    TetrisShapeResult(viable = false, _)

  def prepareShapes(in: PrepareShapes, metadata: Metadata): Future[Result] =
    checkTransitionFuture(metadata, ToPrepareShapes) {
      Future.successful(Result(viable = true, SuccessfulShapeServiceResult))
    }(msg => Future.successful(FailedResult(msg)))

  def getAShape(in: GetAShape, metadata: Metadata): Future[ShapeResult] =
    checkTransitionFuture(metadata, ToGetAShape) {
      Future.successful(ShapeResult(viable = true, SuccessfulShapeServiceResult, Some(ShapeGenerator.makeAShape)))
    }(msg => Future.successful(FailedShapeResult(msg)))

  def getSomeShapes(in: GetSomeShapes, metadata: Metadata): Source[ShapeResult, NotUsed] =
    ToGetSomeShapes.pipe { state =>
      checkTransitionStream(metadata, state) {
        Source
          .tick(0.seconds, in.intervalMs.milliseconds, ())
          .map(_ => ShapeGenerator.makeAShape)
          .zipWithIndex
          .takeWhile {
            case (_, idx) =>
              idx < in.numberOfShapes
          }
          .map {
            case (shape, _) =>
              ShapeResult(viable = true, SuccessfulShapeServiceResult, Some(shape))
          }
          .viaMat(Flow[ShapeResult].map(identity))(Keep.right)
      }(msg => Source.single(FailedShapeResult(msg)))
    }

  def getSomeTetrisShapes(in: GetSomeTetrisShapes, metadata: Metadata): Source[TetrisShapeResult, NotUsed] =
    ToGetSomeTetrisShapes.pipe { state =>
      checkTransitionStream(metadata, state) {
        Source
          .tick(0.seconds, in.intervalMs.milliseconds, ())
          .map(_ => TetrisShapeGenerator.makeATetrisShape(in.dropSpots))
          .zipWithIndex
          .takeWhile {
            case (_, idx) =>
              idx + in.startingIndex < in.startingIndex + in.numberOfShapes
          }
          .map {
            case (shape, _) =>
              TetrisShapeResult(viable = true, SuccessfulShapeServiceResult, Some(shape))
          }
          .viaMat(Flow[TetrisShapeResult].map(identity))(Keep.right)
      }(msg => Source.single(FailedTetrisShapeResult(msg)))
    }

  def releaseShapes(in: ReleaseShapes, metadata: Metadata): Future[Result] =
    checkTransitionFuture(metadata, ToReleaseShapes) {
      Future.successful(Result(viable = true, SuccessfulShapeServiceResult))
    }(msg => Future.successful(FailedResult(msg)))

  private def checkTransitionStream[T](
    metadata: Metadata,
    transition: ProtocolStateTransition
  )(fn: => Source[T, NotUsed])(err: String => Source[T, NotUsed]): Source[T, NotUsed] =
    Source
      .fromFuture(tryTransition(idFromMetadata(metadata), transition))
      .flatMapConcat {
        case None ⇒
          fn

        case Some(message) ⇒
          err(message)
      }

  private def checkTransitionFuture[T](
    metadata: Metadata,
    transition: ProtocolStateTransition
  )(fn: => Future[T])(err: String => Future[T]): Future[T] =
    tryTransition(idFromMetadata(metadata), transition)
      .flatMap {
        case None ⇒
          fn

        case Some(message) ⇒
          err(message)
      }

  private def idFromMetadata(metadata: Metadata): String =
    metadata.getText(HeaderKey.toLowerCase).getOrElse(UUID.randomUUID().toString)
}