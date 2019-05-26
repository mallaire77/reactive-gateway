package com.yoppworks.rxgateway.server

import com.yoppworks.rxgateway.api.{Shape, TetrisShape}
import com.yoppworks.rxgateway.server.ShapeFlowActor._
import com.yoppworks.rxgateway.utils.ChainingSyntax
import akka.actor.{Actor, ActorRef, Props}
import akka.stream.{KillSwitch, KillSwitches}

object ShapeFlowActor {
  case class State(
    regularShapes: Seq[Shape] = Seq.empty,
    tetrisShapes: Seq[TetrisShape] = Seq.empty,
    killswitch: KillSwitch = KillSwitches.shared("shape-flow")
  ) {
    def withRegularShapes(x: Seq[Shape]): State =
      copy(regularShapes = x)

    def withTetrisShapes(x: Seq[TetrisShape]): State =
      copy(tetrisShapes = x)
  }

  sealed trait ShapeType

  object RegularShapeType extends ShapeType

  object TetrisShapeType extends ShapeType

  case class PrepareShapes(numberOfShapesToPrepare: Int)

  case class GetAShape(shapeType: ShapeType, dropSpots: Seq[Int] = Seq.empty)

  case class GetSomeShapes(shapeType: ShapeType, index: Int, consume: Int, dropSpots: Seq[Int] = Seq.empty)

  case class RegularShapes(shapes: Seq[Shape])

  case class TetrisShapes(shapes: Seq[TetrisShape])

  object ReleaseShapes

  object InvalidRange

  def props: Props =
    Props(new ShapeFlowActor)
}

class ShapeFlowActor extends Actor with ShapeGenerator with TetrisShapeGenerator with ChainingSyntax {
  private type Retained[T] = Seq[T]

  private type Consumed[T] = Seq[T]

  private type Partitioned[T] = (Retained[T], Consumed[T])

  private var state = State()

  override def receive: Receive = {
    case PrepareShapes(numberOfShapesToPrepare) =>
      this.prepareRegularShapes(numberOfShapesToPrepare)
      this.prepareTetrisShapes(numberOfShapesToPrepare)

    case GetAShape(shapeType, dropSpots) =>
      sender() ! this.consume(0, 1, dropSpots)(shapeType)

    case GetSomeShapes(shapeType, index, consume, dropSpots) =>
      sender() ! this.consume(index, consume, dropSpots)(shapeType)

    case ReleaseShapes =>
      this.killswitchEngage()
      this.reset()
  }

  private def reset(): Unit =
    state = State()

  private def killswitchEngage(): Unit =
    state.killswitch.shutdown()

  private def consume(index: Int, consume: Int, dropSpots: Seq[Int] = Seq.empty): Any = {
    case RegularShapeType =>
      RegularShapes(this.consumeRegularShapes(0, 1))

    case TetrisShapeType =>
      TetrisShapes(this.consumeTetrisShapes(0, 1).map(_.withDropSpots(dropSpots)))
  }

  private def prepareRegularShapes(numberOfShapesToPrepare: Int): Unit =
    withRegularShapes(Seq.fill(numberOfShapesToPrepare)(ShapeGenerator.makeAShape))

  private def withRegularShapes(shapes: Seq[Shape]): Unit =
    state = state.withRegularShapes(shapes)

  private def prepareTetrisShapes(numberOfShapesToPrepare: Int): Unit =
    withTetrisShapes(Seq.fill(numberOfShapesToPrepare)(TetrisShapeGenerator.makeATetrisShape))

  private def withTetrisShapes(shapes: Seq[TetrisShape]): Unit =
    state = state.withTetrisShapes(shapes)

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
              range.contains(idx - 1)
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
