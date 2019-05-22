package com.yoppworks.rxgateway.server

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import com.typesafe.config.ConfigFactory

import com.yoppworks.rxgateway.api.{ShapeService, ShapeServicePowerApiHandler}
import com.yoppworks.rxgateway.server.lib.GrpcServer

import scala.concurrent.ExecutionContext

object ShapeGrpcServer extends GrpcServer with App {
  lazy val conf = ConfigFactory.load().resolve()

  lazy val name: String =
    conf.getString("akka.http.server.name")

  lazy val interface: String =
    conf.getString("akka.http.server.interface")

  lazy val port: Int =
    conf.getInt("akka.http.server.port")

  implicit lazy val system: ActorSystem = ActorSystem(name)
  implicit lazy val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  lazy val GrpcRejectionHandlers =
    system => ShapeServiceImpl.RejectionHandler(system)

  lazy val GrpcHandler =
    errorHandler => ShapeServicePowerApiHandler.partial(ShapeServiceImpl(), eHandler = errorHandler)

  run()
}