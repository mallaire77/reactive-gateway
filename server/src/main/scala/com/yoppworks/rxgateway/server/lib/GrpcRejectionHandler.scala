package com.yoppworks.rxgateway.server.lib

import akka.actor.ActorSystem

import io.grpc.Status

import scala.util.control.NoStackTrace

object GrpcRejectionHandler {
  case class GrpcRejection(status: Int, message: String) extends Throwable(message) with NoStackTrace

  def errorMapper(implicit system: ActorSystem): PartialFunction[Throwable, Status] = {
    case GrpcRejection(status, _) =>
      Status.fromCodeValue(status)
  }
}
