package com.yoppworks.rxgateway.server

import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Keep, Source}
import akka.grpc.scaladsl.Metadata

import com.yoppworks.rxgateway.api._
import com.yoppworks.rxgateway.server.EnforcedProtocol._

import scala.concurrent.Future
import scala.concurrent.duration._

/** Unit Tests For ShapeServiceImpl */
case class ShapeServiceImpl() extends ShapeService {
  
  final val HeaderKey = "X-USERNAME"

  final val SuccessfulShapeServiceResult = "Success"

  def prepareShapes(metadata: Metadata, in: PrepareShapes): Future[ShapeServiceResult] =
    checkTransitionFuture(metadata, ToReleaseShapes) {
      prepareShapes(in)
    }(err = msg => Future.successful(ShapeServiceResult(viable = false, msg)))

  def prepareShapes(in: PrepareShapes): Future[ShapeServiceResult] =
    Future.successful(ShapeServiceResult(viable = true, SuccessfulShapeServiceResult))

  def getAShape(metadata: Metadata, in: GetAShape): Future[ShapeServiceResult] =
    checkTransitionFuture(metadata, ToReleaseShapes) {
      getAShape(in)
    }(err = msg => Future.successful(ShapeServiceResult(viable = false, msg)))

  def getAShape(in: GetAShape): Future[ShapeServiceResult] =
    Future.successful(ShapeServiceResult(viable = true, SuccessfulShapeServiceResult))

  def getSomeShapes(metadata: Metadata, in: GetSomeShapes): Source[Shape, NotUsed] =
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

  def getSomeTetrisShapes(metadata: Metadata, in: GetSomeTetrisShapes): Source[TetrisShape, NotUsed] =
    checkTransitionStream(metadata, ToReleaseShapes) {
      getSomeTetrisShapes(in)
    }()

  def getSomeTetrisShapes(in: GetSomeTetrisShapes): Source[TetrisShape, NotUsed] =
    Source.empty[TetrisShape]

  def releaseShapes(metadata: Metadata, in: ReleaseShapes): Future[ShapeServiceResult] =
    checkTransitionFuture(metadata, ToReleaseShapes) {
      releaseShapes(in)
    }(err = msg => Future.successful(ShapeServiceResult(viable = false, msg)))

  def releaseShapes(in: ReleaseShapes): Future[ShapeServiceResult] =
    Future {
      ShapeServiceResult()
    }

  private def checkTransitionStream[T](
    metadata: Metadata,
    transition: ProtocolStateTransition
  )(fn: => Source[T, NotUsed])(err: String => Source[T, NotUsed] = (msg: String) => throw new Exception(msg)): Source[T, NotUsed] =
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
  )(fn: => Future[T])(err: String => Future[T] = (msg: String) => throw new Exception(msg)): Future[T] =
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
