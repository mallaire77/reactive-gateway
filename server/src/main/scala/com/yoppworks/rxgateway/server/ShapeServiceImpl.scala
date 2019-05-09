package com.yoppworks.rxgateway.server

import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Keep, Source}
import akka.grpc.scaladsl.Metadata

import com.yoppworks.rxgateway.api._
import com.yoppworks.rxgateway.server.ShapeEnforcedProtocol._
import com.yoppworks.rxgateway.server.ShapeServiceImpl.InvalidStateChange

import io.grpc.Status

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.control.NoStackTrace

case class ShapeServiceImpl() extends ShapeService {
  private type Message = String

  private final val HeaderKey = "X-USERNAME"

  private final val SuccessfulShapeServiceResult = "Success"

  private final val FailedShapeServiceResult: Message => Future[ShapeServiceResult] =
    msg => Future.successful(ShapeServiceResult(viable = false, msg))

  def prepareShapes(in: PrepareShapes, metadata: Metadata): Future[ShapeServiceResult] =
    checkTransitionFuture(metadata, ToReleaseShapes) {
      prepareShapes(in)
    }(FailedShapeServiceResult)

  def prepareShapes(in: PrepareShapes): Future[ShapeServiceResult] =
    Future.successful(ShapeServiceResult(viable = true, SuccessfulShapeServiceResult))

  def getAShape(in: GetAShape, metadata: Metadata): Future[Shape] =
    checkTransitionFuture(metadata, ToReleaseShapes) {
      getAShape(in)
    }()

  def getAShape(in: GetAShape): Future[Shape] =
    Future.successful(ShapeGenerator.makeAShape)

  def getSomeShapes(in: GetSomeShapes, metadata: Metadata): Source[Shape, NotUsed] =
    checkTransitionStream(metadata, ToReleaseShapes) {
      getSomeShapes(in)
    }()

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

  def getSomeTetrisShapes(in: GetSomeTetrisShapes, metadata: Metadata): Source[TetrisShape, NotUsed] =
    checkTransitionStream(metadata, ToReleaseShapes) {
      getSomeTetrisShapes(in)
    }()

  def getSomeTetrisShapes(in: GetSomeTetrisShapes): Source[TetrisShape, NotUsed] =
    Source
      .tick(0.seconds, in.intervalMs.milliseconds, ShapeGenerator.makeATetrisShape(in.dropSpots))
      .zipWithIndex
      .takeWhile {
        case (_, idx) =>
          idx + in.startingIndex < in.startingIndex + in.numberOfShapes
      }
      .map {
        case (shape, _) =>
          shape
      }
      .viaMat(Flow[TetrisShape].map(identity))(Keep.right)

  def releaseShapes(in: ReleaseShapes, metadata: Metadata): Future[ShapeServiceResult] =
    checkTransitionFuture(metadata, ToReleaseShapes) {
      releaseShapes(in)
    }(FailedShapeServiceResult)

  def releaseShapes(in: ReleaseShapes): Future[ShapeServiceResult] =
    Future.successful(ShapeServiceResult(viable = true, SuccessfulShapeServiceResult))

  private def checkTransitionStream[T](
    metadata: Metadata,
    transition: ProtocolStateTransition
  )(fn: => Source[T, NotUsed])(err: String => Source[T, NotUsed] = (_: String) => throw InvalidStateChange): Source[T, NotUsed] =
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
  )(fn: => Future[T])(err: String => Future[T] = (_: String) => throw InvalidStateChange): Future[T] =
    tryTransition(idFromMetadata(metadata), transition)
      .flatMap {
        case None ⇒
          fn

        case Some(message) ⇒
          err(message)
      }

  private def idFromMetadata(metadata: Metadata): String =
    metadata
      .getText(HeaderKey)
      .getOrElse(UUID.randomUUID().toString)
}

object ShapeServiceImpl {
  case object InvalidStateChange extends Throwable with NoStackTrace

  def RejectionHandler: PartialFunction[Throwable, Status] = {
    case InvalidStateChange =>
      Status.FAILED_PRECONDITION
  }
}
