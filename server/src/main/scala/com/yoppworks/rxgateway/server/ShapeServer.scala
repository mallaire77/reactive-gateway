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
    val conf = ConfigFactory.parseString("akka.http.server.preview.enable-http2 = on")
      .withFallback(ConfigFactory.defaultApplication())
    val actorSystem: ActorSystem = ActorSystem("ShapeServer", conf)
    val _ = new ShapeServer(actorSystem).run()
  }
}

class ShapeServer(system: ActorSystem) {
  
  def run() : Future[Http.ServerBinding] = {
    // Akka boot up code
    implicit val sys : ActorSystem = system
    implicit val mat : Materializer = ActorMaterializer()
    implicit val ec : ExecutionContext = sys.dispatcher

    // Configuration values
    val config: com.typesafe.config.Config =
      ConfigFactory.load().resolve()

    val interface: String =
      config.getString("akka.http.interface")

    val port: Int =
      config.getInt("akka.http.port")
    
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
