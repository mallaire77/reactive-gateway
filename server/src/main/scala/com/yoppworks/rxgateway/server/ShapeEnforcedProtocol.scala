package com.yoppworks.rxgateway.server

import scala.concurrent.{ExecutionContext, Future}

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
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
 *
 * To use this, invoke the tryTransition function:
 * {{{  def tryTransition(id: String, transition: ProtocolStateTransition) }}}
 * Pass the unique id of the client to be tracked and the transition to try.
 * The implementation uses an Akka Typed actor for each client that is
 * managed by Akka Typed's Receptionist protocol.
 */
object ShapeEnforcedProtocol {
  
  /**
   * The basic message transmitted to the Actor to try a transition
   */
  final case class CheckTransition(
    name: String,
    transition: ProtocolStateTransition,
    replyTo: ActorRef[ErrorResult]
  )
  
  /**
   * The behavior of the guardian (root, main) actor to which all top level
   * messages are sent
   * @return The Behavior for CheckTransition
   */
  private def guardian: Behavior[CheckTransition] = {
    Behaviors.receive[CheckTransition] {
      case (context, message) ⇒
        message match {
          case m: CheckTransition ⇒
            checkTransition(m)(context)
            Behaviors.same
        }
      }
  }
  
  private val system : ActorSystem[CheckTransition] =
    ActorSystem(guardian, "protocol-enforcement")
  
  implicit val timeout: Timeout = Timeout(60.seconds)
  implicit val ec: ExecutionContext = system.executionContext
  implicit val scheduler : Scheduler = system.scheduler
  
  sealed trait ProtocolStateTransition

  case object ToPrepareShapes extends ProtocolStateTransition
  case object ToGetAShape extends ProtocolStateTransition
  case object ToGetSomeShapes extends ProtocolStateTransition
  case object ToGetSomeTetrisShapes extends ProtocolStateTransition
  case object ToReleaseShapes extends ProtocolStateTransition
  
  type ErrorResult = Option[String]
  
  /** Client Interface
   * Clients of this object should invoke this function to transition their
   * client to a new state.
   * @param id The unique name of the session/client/id for whom the protocol
   *           enforcement is being done.
   * @param transition The transition to try next which is a value extended
   *                   from the `ProtocolStateTransition` sealed trait.
   * @return `Future[ErrorState]` The future result will be either
   * `None` if the transition was permitted, or
   * `Some[String]` if there is an error message to report
   */
  def tryTransition(id: String, transition: ProtocolStateTransition)
  : Future[ErrorResult] = {
    system ?[ErrorResult] { actorRef ⇒
      CheckTransition(id, transition, actorRef)
    }
  }
  
  /**
   * An internal utility to find the actor,  or create it if necessary, and
   * send the CheckTransition message to it. This is used by the guardian's
   * behavior to dispatch to the correct actor
   * @param check The message to dispatch
   * @param context The actor context
   */
  private final def checkTransition(
    check: CheckTransition
  )(implicit context: ActorContext[_]
  ) : Unit = {
    val serviceKey = ServiceKey[ CheckTransition ](check.name)
    val futureListing : Future[ Receptionist.Listing ] = {
      context.system.receptionist ? {
        replyTo => Receptionist.find[ CheckTransition ](serviceKey, replyTo)
      }
    }
    val _ = futureListing.map { listing ⇒
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
  
  // The initial behavior of the protocol enforcement actor ensuring that
  // only the ToPrepareShapes transition is permitted as the first transition.
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
  
  // Transition logic for the prepareShapes state
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
  
  // Transition logic for the getAShape state
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

  // Transition logic for the getSomeShapes state
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
  
  // Transition logic for the getSomeTetrisShapes state
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

  // Transition logic for releaseShapes: stop the actor
  private def releaseShapes: Behavior[CheckTransition] = {
    Behaviors.receive { (_, _) ⇒
      Behaviors.stopped
    }
  }
}
