package com.yoppworks.rxgateway.server.lib

import akka.actor.ActorSystem

import io.grpc.Status

import scala.util.control.NoStackTrace

object ErrorHandler {
  case class GrpcError(status: Int, message: String) extends Throwable(message) with NoStackTrace

  def errorMapper(implicit system: ActorSystem): PartialFunction[Throwable, Status] = {
    case GrpcError(status, _) =>
      Status.fromCodeValue(status)
  }
}
