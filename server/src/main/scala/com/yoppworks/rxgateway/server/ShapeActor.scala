package com.yoppworks.rxgateway.server

import com.yoppworks.rxgateway.api.{Shape, TetrisShape}
import com.yoppworks.rxgateway.server.ShapeActor.{GetAShape, InvalidRange, PrepareShape, RegularShapeType, RegularShapes, State, TetrisShapeType, TetrisShapes}
import com.yoppworks.rxgateway.utils.ChainingSyntax

import akka.actor.{Actor, ActorSystem}

object ShapeActor {
  object InvalidRange

  case class State(regularShapes: Seq[Shape] = Seq.empty, tetrisShapes: Seq[TetrisShape] = Seq.empty)

  sealed trait ShapeType

  object RegularShapeType extends ShapeType

  object TetrisShapeType extends ShapeType

  case class PrepareShape(numberOfShapesToPrepare: Int, shapeType: ShapeType, dropSpot: Option[Int] = None)

  case class GetAShape(shapeType: ShapeType)

  case class RegularShapes(shapes: Seq[Shape])

  case class TetrisShapes(shapes: Seq[TetrisShape])
}

case class ShapeActor(implicit system: ActorSystem) extends Actor with ChainingSyntax {
  private type Retained[T] = Seq[T]

  private type Consumed[T] = Seq[T]

  private type Partitioned[T] = (Retained[T], Consumed[T])

  private var state = State()

  override def receive: Receive = {
    case PrepareShape(numberOfShapesToPrepare, shapeType, dropSpot) =>
      shapeType match {
        case RegularShapeType =>
          withRegularShapes(numberOfShapesToPrepare)

        case TetrisShapeType =>
          withTetrisShapes(numberOfShapesToPrepare, dropSpot.getOrElse(0))
      }

    case GetAShape(shapeType) =>
      shapeType match {
        case RegularShapeType =>
          sender() ! RegularShapes(consumeRegularShapes(0, 1))

        case TetrisShapeType =>
          sender() ! TetrisShapeGenerator(consumeTetrisShapes(0, 1))
      }
  }

  private def withRegularShapes(numberOfShapesToPrepare: Int): Unit =
    withRegularShapes(Seq.fill(numberOfShapesToPrepare)(ShapeGenerator.makeAShape))

  private def withRegularShapes(shapes: Seq[Shape]): Unit =
    state.copy(regularShapes = shapes)

  private def withTetrisShapes(numberOfShapesToPrepare: Int, dropSpot: Int): Unit =
    withTetrisShapes(Seq.fill(numberOfShapesToPrepare)(ShapeGenerator.makeATetrisShape(dropSpot)))

  private def withTetrisShapes(shapes: Seq[TetrisShape]): Unit =
    state.copy(tetrisShapes = shapes)

  private def consumeRegularShapes(index: Int, consume: Int): Seq[Shape] =
    consumeShapes(index, consume)(state.regularShapes).pipe {
      case Right((retained, consumed)) =>
        withRegularShapes(retained)
        consumed

      case Left(_) =>
        Seq.empty
    }

  private def consumeTetrisShapes(index: Int, consume: Int): Seq[TetrisShape] =
    consumeShapes(index, consume)(state.tetrisShapes).pipe {
      case Right((retained, consumed)) =>
        withTetrisShapes(retained)
        consumed

      case Left(_) =>
        Seq.empty
    }

  private def consumeShapes[T](index: Int, consume: Int)(seq: Seq[T]): Either[InvalidRange.type, Partitioned[T]] =
    if (isConsumeWithinRange(index, consume)(seq))
      (index to index + (consume - 1)).pipe { range =>
        val (consumed, retained) =
          seq.zipWithIndex.partition {
            case (_, idx) =>
              range.contains(idx)
          }
        Right((dropIdx(retained), dropIdx(consumed)))
      }
    else
      Left(InvalidRange)


  private def isConsumeWithinRange[T](index: Int, consume: Int)(seq: Seq[T]): Boolean =
    seq.nonEmpty && index > 0 && index + (consume - 1) < seq.length - 1

  private def dropIdx[T](seq: Seq[(T, Int)]): Seq[T] =
    seq.map(_._1)
}
