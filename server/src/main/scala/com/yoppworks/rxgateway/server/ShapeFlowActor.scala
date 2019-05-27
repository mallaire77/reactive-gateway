package com.yoppworks.rxgateway.server

import com.yoppworks.rxgateway.api.{Shape, TetrisShape}
import com.yoppworks.rxgateway.server.ShapeFlowActor._
import com.yoppworks.rxgateway.utils.ChainingSyntax

import akka.actor.{Actor, Props}
import akka.stream.{KillSwitches, SharedKillSwitch}

object ShapeFlowActor {
  type ShapeResult[T] = Either[ShapeError, T]

  case class State(
    regularShapes: Seq[Shape] = Seq.empty,
    tetrisShapes: Seq[TetrisShape] = Seq.empty,
    killswitch: SharedKillSwitch = KillSwitches.shared("shape-flow")
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

  case class GetAShape(shapeType: ShapeType, index: Int, dropSpots: Seq[Int] = Seq.empty)

  case class GetSomeShapes(shapeType: ShapeType, index: Int, consume: Int, dropSpots: Seq[Int] = Seq.empty)

  case class RegularShapes(shapes: Seq[Shape], killswitch: SharedKillSwitch)

  case class TetrisShapes(shapes: Seq[TetrisShape], killswitch: SharedKillSwitch)

  object ReleaseShapes

  sealed abstract class ShapeError(val msg: String)

  object InvalidRange extends ShapeError("Invalid range")

  def props: Props =
    Props(new ShapeFlowActor)
}

class ShapeFlowActor extends Actor with RegularShapeGenerator with TetrisShapeGenerator with ChainingSyntax {
  private type Retained[T] = Seq[T]

  private type Consumed[T] = Seq[T]

  private type Partitioned[T] = (Retained[T], Consumed[T])

  private var state = State()

  override def receive: Receive = {
    case PrepareShapes(numberOfShapesToPrepare) =>
//      println(s"ShapeFlowActor: PrepareShapes($numberOfShapesToPrepare)")
//      println(s"ShapeFlowActor: State: Before: PrepareShapes $state")
      this.prepareRegularShapes(numberOfShapesToPrepare)
      this.prepareTetrisShapes(numberOfShapesToPrepare)
//      println(s"ShapeFlowActor: State: After: PrepareShapes $state")
      sender() ! ()

    case GetAShape(shapeType, index, dropSpots) =>
//      println(s"ShapeFlowActor: GetAShape($shapeType,$index,$dropSpots)")
      this.consumeShapes(index, 1, dropSpots)(shapeType).pipe { msg =>
//        println(s"ShapeFlowActor: Responding: GetAShape $msg")
//        println(s"ShapeFlowActor: State: GetAShape $state")
        sender() ! msg
      }

    case GetSomeShapes(shapeType, index, consume, dropSpots) =>
//      println(s"ShapeFlowActor: GetSomeShapes($shapeType,$index,$consume,$dropSpots)")
      this.consumeShapes(index, consume, dropSpots)(shapeType).pipe { msg =>
//        println(s"ShapeFlowActor: Responding: GetSomeShapes $msg")
//        println(s"ShapeFlowActor: State: GetSomeShapes $state")
        sender() ! msg
      }

    case ReleaseShapes =>
//      println(s"ShapeFlowActor: ReleaseShapes")
      this.killswitchEngage()
      this.reset()
      sender() ! ()
  }

  private def reset(): Unit =
    state = State()

  private def killswitchEngage(): Unit =
    state.killswitch.shutdown()

  private def prepareRegularShapes(numberOfShapesToPrepare: Int): Unit =
    withRegularShapes(Seq.fill(numberOfShapesToPrepare)(RegularShapeGenerator.makeAShape))

  private def withRegularShapes(shapes: Seq[Shape]): Unit =
    state = state.withRegularShapes(shapes)

  private def prepareTetrisShapes(numberOfShapesToPrepare: Int): Unit =
    withTetrisShapes(Seq.fill(numberOfShapesToPrepare)(TetrisShapeGenerator.makeATetrisShape))

  private def withTetrisShapes(shapes: Seq[TetrisShape]): Unit =
    state = state.withTetrisShapes(shapes)

  private def consumeShapes(index: Int, consume: Int, dropSpots: Seq[Int] = Seq.empty): ShapeType => Any = {
    case RegularShapeType =>
      this.partition(index, consume)(state.regularShapes).map {
        case (retained, consumed) =>
          withRegularShapes(retained)
          RegularShapes(consumed, state.killswitch)
      }

    case TetrisShapeType =>
      this.partition(index, consume)(state.tetrisShapes).map {
        case (retained, consumed) =>
          withTetrisShapes(retained)
          TetrisShapes(consumed, state.killswitch)
      }
  }

  private def partition[T](
    index: Int,
    consume: Int
  )(seq: Seq[T]): Either[ShapeError, Partitioned[T]] =
    (consume - 1).pipe { adjustedConsume =>
      if (isConsumeWithinRange(index, adjustedConsume)(seq))
        (index to index + adjustedConsume).pipe { range =>
          seq
            .zipWithIndex
            .partition {
              case (_, idx) =>
                range.contains(idx)
            }
            .pipe {
              case (consumed, retained) =>
                Right(dropIdx(retained) -> dropIdx(consumed))
            }
        }
      else
        Left(InvalidRange)
    }

  private def isConsumeWithinRange[T](index: Int, consume: Int)(seq: Seq[T]): Boolean =
    seq.nonEmpty && index >= 0 && consume >= 0 && index + consume <= seq.length - 1

  private def dropIdx[T](seq: Seq[(T, Int)]): Seq[T] =
    seq.map(_._1)
}
