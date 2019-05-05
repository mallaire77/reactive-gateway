package com.yoppworks.rxgateway.server

import scala.concurrent.{ExecutionContext, Future}

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.receptionist.ServiceKey
import akka.util.Timeout
import scala.concurrent.duration._

import akka.actor.Scheduler

/** API Protocol Enforcer for ShapeServer
 *
 * This enforces an interaction protocol that follows this regex-ish pattern:
 *  PrepareShapes{1},
 *  getAShape*,
 *  (getSomeShapes*|getSomeTetrisShapes*),
 *  ReleaseShapes{1}
 */
object EnforcedProtocol {
  
  final case class CheckTransition(
    name: String,
    transition: ProtocolStateTransition,
    replyTo: ActorRef[ErrorResult]
  )
  
  private val main: Behavior[CheckTransition] = {
    Behaviors.setup[CheckTransition] { implicit context: ActorContext[_] ⇒
      Behaviors.receiveMessage[CheckTransition] { message =>
        checkTransition(message)
        Behaviors.same
      }
      Behaviors.receiveSignal {
        case (_, Terminated(_)) =>
          Behaviors.stopped
      }
    }
  }
  
  val system : ActorSystem[ CheckTransition ] =
    ActorSystem(main, "protocol-enforcement")
  
  implicit val timeout: Timeout = Timeout(10.seconds)
  
  sealed trait ProtocolStateTransition

  case object ToPrepareShapes extends ProtocolStateTransition
  case object ToGetAShape extends ProtocolStateTransition
  case object ToGetSomeShapes extends ProtocolStateTransition
  case object ToGetSomeTetrisShapes extends ProtocolStateTransition
  case object ToReleaseShapes extends ProtocolStateTransition
  
  type ErrorResult = Option[String]
  
  /** Client Interface
   * Clients of this object should invoke this function to transition their
   * client to a new state. The function returns a
   * @param id The unique name of the session/client/id for whom the protocol
   *           enforcement is being done.
   * @param transition The transition to try next
   * @return Future[ErrorState] - The future result will be
   *         - None if the transition was permitted
   *         - Some[String] if there is an error message to report
   */
  def tryTransition(id: String, transition: ProtocolStateTransition)
  : Future[ErrorResult] = {
    implicit val scheduler : Scheduler = system.scheduler
    system ?[ErrorResult] { actorRef ⇒
      CheckTransition(id, transition, actorRef)
    }
  }
  
  
  private final def checkTransition(
    check: CheckTransition
  )(implicit context: ActorContext[_]
  ) : Future[Unit] = {
    implicit val scheduler : Scheduler = context.system.scheduler
    implicit val executor : ExecutionContext = context.system.executionContext
    val serviceKey = ServiceKey[ CheckTransition ](check.name)
    val futureListing : Future[ Receptionist.Listing ] = {
      context.system.receptionist ? {
        replyTo => Receptionist.find[ CheckTransition ](serviceKey, replyTo)
      }
    }
    futureListing.map { listing ⇒
      val actorRefSet = listing.serviceInstances[ CheckTransition ](
        listing.key.asInstanceOf[ ServiceKey[ CheckTransition ] ]
      )
      val actorRef = if (actorRefSet.isEmpty) {
        val ref = context.spawn(initialBehavior, serviceKey.id)
        context.system.receptionist ! Receptionist.register(serviceKey, ref)
        ref
      } else {
        actorRefSet.head
      }
      actorRef ! check
    }
  }
  
  private def initialBehavior: Behavior[CheckTransition] = {
    Behaviors.receive[ CheckTransition ] { (_, message ) ⇒
      message match {
        case CheckTransition(_, transition, replyTo) ⇒
          transition match {
            case ToPrepareShapes ⇒
              replyTo ! None
              prepareShapes
            case ToGetAShape ⇒
              replyTo ! Some("First transition must be to PrepareShapes")
              Behaviors.same
            case ToGetSomeShapes ⇒
              replyTo ! Some("First transition must be to PrepareShapes")
              Behaviors.same
            case ToGetSomeTetrisShapes ⇒
              replyTo ! Some("First transition must be to PrepareShapes")
              Behaviors.same
            case ToReleaseShapes ⇒
              replyTo ! Some("First transition must be to PrepareShapes")
              Behaviors.same
          }
      }
    }
  }
  
  private def prepareShapes: Behavior[CheckTransition] = {
    Behaviors.receive {(_, message) ⇒
      message match {
        case CheckTransition(_, transition, replyTo) ⇒
          transition match {
            case ToPrepareShapes ⇒
              replyTo ! Some("Only one prepareShapes is permitted")
              Behaviors.same
            case ToGetAShape ⇒
              replyTo ! None
              getAShape
            case ToGetSomeShapes ⇒
              replyTo ! None
              getSomeShapes
            case ToGetSomeTetrisShapes ⇒
              replyTo ! None
              getSomeTetrisShapesState
            case ToReleaseShapes ⇒
              replyTo ! None
              releaseShapes
          }
      }
    }
  }
  
  private def getAShape: Behavior[CheckTransition] = {
    Behaviors.receive {(_, message) ⇒
      message match {
        case CheckTransition(_, transition, replyTo) ⇒
          transition match {
            case ToPrepareShapes ⇒
              replyTo ! Some("Already prepared, releaseShapes first")
              Behaviors.same
            case ToGetAShape ⇒
              replyTo ! None
              getAShape
            case ToGetSomeShapes ⇒
              replyTo ! None
              getSomeShapes
            case ToGetSomeTetrisShapes ⇒
              replyTo ! None
              getSomeTetrisShapesState
            case ToReleaseShapes ⇒
              replyTo ! None
              releaseShapes
          }
      }
    }
  }

  private def getSomeShapes: Behavior[CheckTransition] = {
    Behaviors.receive {(_, message) ⇒
      message match {
        case CheckTransition(_, transition, replyTo) ⇒
          transition match {
            case ToPrepareShapes ⇒
              replyTo ! Some("Already prepared, releaseShapes first")
              Behaviors.same
            case ToGetAShape ⇒
              replyTo ! Some("getAShape not permitted after getSomeShapes")
              Behaviors.same
            case ToGetSomeShapes ⇒
              replyTo ! None
              getSomeShapes
            case ToGetSomeTetrisShapes ⇒
              replyTo ! Some(
                "getSomeTetrisShapes not permitted after getSomeShapes")
              getSomeTetrisShapesState
            case ToReleaseShapes ⇒
              replyTo ! None
              releaseShapes
          }
      }
    }
  }

  private def getSomeTetrisShapesState: Behavior[CheckTransition] = {
    Behaviors.receive { (_, message) ⇒
      message match {
        case CheckTransition(_, transition, replyTo) ⇒
          transition match {
            case ToPrepareShapes ⇒
              replyTo ! Some("Already prepared, releaseShapes first")
              Behaviors.same
            case ToGetAShape ⇒
              replyTo ! Some("getAShape not permitted after getSomeShapes")
              getAShape
            case ToGetSomeShapes  ⇒
              replyTo ! Some("getSomeShapes not permitted after getSomeTetrisShapes")
              getSomeShapes
            case ToGetSomeTetrisShapes  ⇒
              replyTo ! None
              getSomeTetrisShapesState
            case ToReleaseShapes ⇒
              replyTo ! None
              releaseShapes
          }
      }
    }
  }

  private def releaseShapes: Behavior[CheckTransition] = {
    Behaviors.receive { (_, _) ⇒
      Behaviors.stopped
    }
  }
}
