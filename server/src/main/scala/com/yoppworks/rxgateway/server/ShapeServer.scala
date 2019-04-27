package com.yoppworks.rxgateway.server

import scala.concurrent.{ExecutionContext, Future}

import akka.actor.ActorSystem
import akka.http.scaladsl.UseHttp2.Always
import akka.http.scaladsl.{Http, HttpConnectionContext}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.{ActorMaterializer, Materializer}

import com.typesafe.config.ConfigFactory
import com.yoppworks.rxgateway.api.ShapeServiceHandler

object ShapeServer {
  def main(args: Array[String]): Unit = {
    val conf = ConfigFactory.load().resolve()

    val name: String =
      conf.getString("akka.http.server.name")

    val interface: String =
      conf.getString("akka.http.server.interface")

    val port: Int =
      conf.getInt("akka.http.server.port")

    implicit val actorSystem: ActorSystem =
      ActorSystem(name, conf)

    new ShapeServer(interface, port).run()
  }
}

class ShapeServer(interface: String, port: Int)(implicit system: ActorSystem) {
  def run(): Future[Http.ServerBinding] = {
    // Akka boot up code
    implicit val mat: Materializer = ActorMaterializer()
    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
    
    // Create service handlers
    val service : HttpRequest => Future[HttpResponse] =
      ShapeServiceHandler(ShapeServiceImpl())
    
    // Bind service handler servers to configured values
    val binding =
      Http().bindAndHandleAsync(
        service,
        interface = interface,
        port = port,
        connectionContext = HttpConnectionContext(http2 = Always))
    
    // report successful binding
    binding.foreach {binding =>
      println(s"gRPC over HTTP/2 server bound to: ${binding.localAddress}")
    }
    
    binding
  }
}
