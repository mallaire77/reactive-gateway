package com.yoppworks.rxgateway.server

import akka.actor.{Actor, ActorRef, Props}

import com.yoppworks.rxgateway.models.Id
import com.yoppworks.rxgateway.server.ShapeFlowParentActor.Message
import com.yoppworks.rxgateway.utils.ChainingSyntax

import scala.collection.mutable

object ShapeFlowParentActor {
  case class Message(id: Id, message: Any)

  def props: Props =
    Props(new ShapeFlowParentActor)
}

class ShapeFlowParentActor extends Actor with ChainingSyntax {
  private val actors: mutable.Map[Id, ActorRef] =
    mutable.Map[Id, ActorRef]()

  override def receive: Receive = {
    case Message(id, message) =>
      actors.getOrElse(id, createShapeFlowActor(id)) forward message
  }

  private def createShapeFlowActor(id: String): ActorRef =
    context
      .actorOf(ShapeFlowActor.props, id)
      .pipe { ref =>
        actors += id -> ref
        ref
      }
}
