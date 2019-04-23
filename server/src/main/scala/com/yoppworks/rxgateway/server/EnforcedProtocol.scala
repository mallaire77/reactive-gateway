package com.yoppworks.rxgateway.server

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}

object EnforcedProtocol {
  
  def apply(id: String) : Behavior[EPTransition] =
    Behaviors.setup(context => new EnforcedProtocol(context, id))
  
  sealed trait EPTransition {
    def replyTo : ActorRef[ Either[ String, State ] ]
  }
  
  type State = Behavior[ EPTransition ]
  
  case class TransitionToPrepareShapes(
    replyTo : ActorRef[ Either[ String, State ] ]
  ) extends EPTransition
  
  case class TransitionToGetAShape(
    replyTo : ActorRef[ Either[ String, State ] ]
  ) extends EPTransition
  
  case class TransitionToGetSomeShapes(
    replyTo : ActorRef[ Either[ String, State ] ]
  ) extends EPTransition
  
  case class TransitionToGetSomeTetrisShapes(
    replyTo : ActorRef[ Either[ String, State ] ]
  ) extends EPTransition
  
  case class TransitionToReleaseShapes(
    replyTo : ActorRef[ Either[ String, State ] ]
  ) extends EPTransition
  
}


class EnforcedProtocol(context: ActorContext[EnforcedProtocol.EPTransition], id: String)
  extends
  AbstractBehavior[EnforcedProtocol.EPTransition] {

  import EnforcedProtocol._
  
  context.log.info(s"ConventionEnforcement for $id starting")
  
  override def onMessage(message: EnforcedProtocol.EPTransition)
  : Behavior[EnforcedProtocol.EPTransition] = {
    message match {
      case TransitionToPrepareShapes(replyTo) ⇒
        replyTo ! Right(prepareShapes)
        prepareShapes
      case TransitionToGetAShape(replyTo) ⇒
        replyTo ! Left("First transition must be to PrepareShapes")
        Behaviors.same
      case TransitionToGetSomeShapes(replyTo) ⇒
        replyTo ! Left("First transition must be to PrepareShapes")
        Behaviors.same
      case TransitionToGetSomeTetrisShapes(replyTo) ⇒
        replyTo ! Left("First transition must be to PrepareShapes")
        Behaviors.same
      case TransitionToReleaseShapes(replyTo) ⇒
        replyTo ! Left("First transition must be to PrepareShapes")
        Behaviors.same
    }
  }
  
  override def onSignal: PartialFunction[Signal,
    Behavior[EnforcedProtocol.EPTransition]] = {
    case PostStop =>
      context.log.info(s"ConventionEnforcement for $id stopped")
      Behaviors.same
  }

  private val prepareShapes: Behavior[EPTransition] = {
    Behaviors.receive { (context, message) ⇒
      message match {
        case TransitionToPrepareShapes(replyTo) ⇒
          replyTo ! Left("Only one prepareShapes is permitted")
          Behaviors.same
        case TransitionToGetAShape(replyTo) ⇒
          replyTo ! Right(getAShape)
          getAShape
        case TransitionToGetSomeShapes(replyTo) ⇒
          replyTo ! Right(getSomeShapes)
          getSomeShapes
        case TransitionToGetSomeTetrisShapes(replyTo) ⇒
          replyTo ! Right(getSomeTetrisShapesState)
          getSomeTetrisShapesState
        case TransitionToReleaseShapes(replyTo) ⇒
          replyTo ! Right(releaseShapes)
          releaseShapes
      }
    }
  }
  
  private val getAShape: Behavior[EPTransition] = {
    Behaviors.receive { (context, message) ⇒
      message match {
        case TransitionToPrepareShapes(replyTo) ⇒
          replyTo ! Left("Already prepared, releaseShapes first")
          Behaviors.same
        case TransitionToGetAShape(replyTo) ⇒
          replyTo ! Right(getAShape)
          getAShape
        case TransitionToGetSomeShapes(replyTo) ⇒
          replyTo ! Right(getSomeShapes)
          getSomeShapes
        case TransitionToGetSomeTetrisShapes(replyTo) ⇒
          replyTo ! Right(getSomeTetrisShapesState)
          getSomeTetrisShapesState
        case TransitionToReleaseShapes( replyTo) ⇒
          replyTo ! Right(releaseShapes)
          Behaviors.stopped
      }
    }
  }
  
  private val getSomeShapes: Behavior[EPTransition] = {
    Behaviors.receive { (context, message) ⇒
      message match {
        case TransitionToPrepareShapes( replyTo) ⇒
          replyTo ! Left("Already prepared, releaseShapes first")
          Behaviors.same
        case TransitionToGetAShape( replyTo) ⇒
          replyTo ! Left("getAShape not permitted after getSomeShapes")
          getAShape
        case TransitionToGetSomeShapes( replyTo) ⇒
          replyTo ! Right(getSomeShapes)
          getSomeShapes
        case TransitionToGetSomeTetrisShapes( replyTo) ⇒
          replyTo ! Left(
            "getSomeTetrisShapes not permitted after getSomeShapes")
          getSomeTetrisShapesState
        case TransitionToReleaseShapes( replyTo) ⇒
          replyTo ! Right(releaseShapes)
          Behaviors.stopped
      }
    }
  }
  
  private val getSomeTetrisShapesState: Behavior[EPTransition] = {
    Behaviors.receive { (context, message) ⇒
      message match {
        case TransitionToPrepareShapes( replyTo) ⇒
          replyTo ! Left("Already prepared, releaseShapes first")
          Behaviors.same
        case TransitionToGetAShape( replyTo) ⇒
          replyTo ! Left("getAShape not permitted after getSomeShapes")
          getAShape
        case TransitionToGetSomeShapes( replyTo) ⇒
          replyTo ! Left("getSomeShapes not permitted after getSomeTetrisShapes")
          getSomeShapes
        case TransitionToGetSomeTetrisShapes( replyTo) ⇒
          replyTo ! Right(getSomeTetrisShapesState)
          getSomeTetrisShapesState
        case TransitionToReleaseShapes( replyTo) ⇒
          replyTo ! Right(releaseShapes)
          releaseShapes
      }
    }
  }
  
  private val releaseShapes: Behavior[EPTransition] = {
    Behaviors.receive { (context, message) ⇒
      Behaviors.stopped
    }
  }
}
