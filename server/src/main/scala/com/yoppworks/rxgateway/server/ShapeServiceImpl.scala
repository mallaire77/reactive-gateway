package com.yoppworks.rxgateway.server

import java.util.UUID

import akka.NotUsed
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.stream.scaladsl.{Flow, Keep, Source}
import akka.grpc.scaladsl.Metadata

import com.yoppworks.rxgateway.api._
import com.yoppworks.rxgateway.models.Id
import com.yoppworks.rxgateway.server.ShapeEnforcedProtocol._
import com.yoppworks.rxgateway.server.ShapeFlowParentActor.Message
import com.yoppworks.rxgateway.utils.ChainingSyntax

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

case class ShapeServiceImpl()(implicit ec: ExecutionContext, system: ActorSystem)
  extends ShapeServicePowerApi with RegularShapeGenerator with TetrisShapeGenerator with ChainingSyntax {
  private type Message = String

  private final val HeaderKey = "X-USERNAME"

  private final val SuccessfulShapeServiceResult = "Success"

  private final val FailedResult: Message => Result =
    Result(viable = false, _)

  private final val FailedShapeResult: Message => ShapeResult =
    ShapeResult(viable = false, _)

  private final val FailedTetrisShapeResult: Message => TetrisShapeResult =
    TetrisShapeResult(viable = false, _)

  private final val ShapeFlowStateActor =
    system.actorOf(ShapeFlowParentActor.props)

  def prepareShapes(in: PrepareShapes, metadata: Metadata): Future[Result] =
    checkTransitionFuture(metadata, ToPrepareShapes) { id =>
      Message(id, ShapeFlowActor.PrepareShapes(in.numberOfShapesToPrepare)).pipe { msg =>
        (ShapeFlowStateActor ? msg).map { _ =>
          Result(viable = true, SuccessfulShapeServiceResult)
        }
      }
    }(msg => Future.successful(FailedResult(msg)))

  def getAShape(in: GetAShape, metadata: Metadata): Future[ShapeResult] =
    checkTransitionFuture(metadata, ToGetAShape) { id =>
      Message(id, ShapeFlowActor.GetAShape(ShapeFlowActor.RegularShapeType, in.index)).pipe { msg =>
        (ShapeFlowStateActor ? msg)
          .mapTo[Either[ShapeFlowActor.ShapeError, ShapeFlowActor.RegularShapes]]
          .map {
            case Right(result) =>
              ShapeResult(viable = true, SuccessfulShapeServiceResult, result.shapes.headOption)

            case Left(error) =>
              FailedShapeResult(error.msg)
          }
      }
    }(msg => Future.successful(FailedShapeResult(msg)))

  def getSomeShapes(in: GetSomeShapes, metadata: Metadata): Source[ShapeResult, NotUsed] =
    ToGetSomeShapes.pipe { state =>
      checkTransitionStream(metadata, state) { id =>
        Message(id, ShapeFlowActor.GetSomeShapes(ShapeFlowActor.RegularShapeType, in.startingIndex, in.numberOfShapes))
          .pipe { msg =>
            (ShapeFlowStateActor ? msg)
              .mapTo[ShapeFlowActor.ShapeResult[ShapeFlowActor.RegularShapes]]
              .pipe { response =>
                Source
                  .fromFuture(response)
                  .flatMapConcat {
                    case Right(result) =>
                      Source
                        .fromIterator(() => result.shapes.toIterator)
                        .throttle(1, in.intervalMs.milliseconds)
                        .via(result.killswitch.flow)
                        .map { shape =>
                          ShapeResult(viable = true, SuccessfulShapeServiceResult, Some(shape))
                        }
                        .viaMat(Flow[ShapeResult].map(identity))(Keep.right)

                    case Left(error) =>
                      Source.single(FailedShapeResult(error.msg))
                  }
              }
          }
      }(msg => Source.single(FailedShapeResult(msg)))
    }

  def getSomeTetrisShapes(in: GetSomeTetrisShapes, metadata: Metadata): Source[TetrisShapeResult, NotUsed] =
    ToGetSomeTetrisShapes.pipe { state =>
      checkTransitionStream(metadata, state) { id =>
        Message(id, ShapeFlowActor.GetSomeShapes(ShapeFlowActor.TetrisShapeType, in.startingIndex, in.numberOfShapes))
          .pipe { msg =>
            (ShapeFlowStateActor ? msg)
              .mapTo[ShapeFlowActor.ShapeResult[ShapeFlowActor.TetrisShapes]]
              .pipe { response =>
                Source
                  .fromFuture(response)
                  .flatMapConcat {
                    case Right(result) =>
                      Source
                        .fromIterator(() => result.shapes.toIterator)
                        .throttle(1, in.intervalMs.milliseconds)
                        .via(result.killswitch.flow)
                        .map { shape =>
                          TetrisShapeResult(viable = true, SuccessfulShapeServiceResult, Some(shape))
                        }
                        .viaMat(Flow[TetrisShapeResult].map(identity))(Keep.right)

                    case Left(error) =>
                      Source.single(FailedTetrisShapeResult(error.msg))
                  }
              }
          }
      }(msg => Source.single(FailedTetrisShapeResult(msg)))
    }

  def releaseShapes(in: ReleaseShapes, metadata: Metadata): Future[Result] =
    checkTransitionFuture(metadata, ToReleaseShapes) { id =>
      Message(id, ShapeFlowActor.ReleaseShapes).pipe { msg =>
        (ShapeFlowStateActor ? msg).map { _ =>
          Result(viable = true, SuccessfulShapeServiceResult)
        }
      }
    }(msg => Future.successful(FailedResult(msg)))

  private def checkTransitionStream[T](
    metadata: Metadata,
    transition: ProtocolStateTransition
  )(fn: Id => Source[T, NotUsed])(err: String => Source[T, NotUsed]): Source[T, NotUsed] =
    idFromMetadata(metadata).pipe { id =>
      Source
        .fromFuture(tryTransition(id, transition))
        .flatMapConcat {
          case None ⇒
            fn(id)

          case Some(message) ⇒
            err(message)
        }
    }

  private def checkTransitionFuture[T](
    metadata: Metadata,
    transition: ProtocolStateTransition
  )(fn: Id => Future[T])(err: String => Future[T]): Future[T] =
    idFromMetadata(metadata).pipe { id =>
      tryTransition(id, transition)
        .flatMap {
          case None ⇒
            fn(id)

          case Some(message) ⇒
            err(message)
        }
    }

  private def idFromMetadata(metadata: Metadata): String =
    metadata.getText(HeaderKey.toLowerCase).getOrElse(UUID.randomUUID().toString)
}