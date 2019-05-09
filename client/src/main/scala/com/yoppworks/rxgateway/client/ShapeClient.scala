package com.yoppworks.rxgateway.client

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

import akka.grpc.GrpcClientSettings
import akka.grpc.scaladsl.{SingleResponseRequestBuilder, StreamResponseRequestBuilder}
import com.yoppworks.rxgateway.api._

object ShapeClient {
  
  // Main program
  def main(args : Array[ String ]) : Unit = {
    // Boot akka
    implicit val sys : ActorSystem = ActorSystem("ShapeClient")
    implicit val mat : ActorMaterializer = ActorMaterializer()
    implicit val ec : ExecutionContextExecutor = sys.dispatcher
  
    // Take details how to connect to the service from the config.
    val clientSettings = GrpcClientSettings
      .connectToServiceAt(host = "localhost", port = 8080)
      .withConnectionAttempts(3)
      .withTls(enabled = false)
  
    // Create a client-side stub for the service
    val client : ShapeServiceClient = ShapeServiceClient(clientSettings)
  
    val name = "scala-client"
  
    def adornSingle[ R, T ](srb : SingleResponseRequestBuilder[ R, T ])
    : SingleResponseRequestBuilder[ R, T ] = {
      srb.addHeader("X-USERNAME", name)
    }
  
    def adornStream[ R, T ](srb : StreamResponseRequestBuilder[ R, T ])
    : StreamResponseRequestBuilder[ R, T ] = {
      srb.addHeader("X-USERNAME", name)
    }
  
    val f1 = for {
      r1 <- adornSingle(client.prepareShapes())
        .invokeWithMetadata(PrepareShapes(numberOfShapesToPrepare = 100))
      r2 ← adornSingle(client.getAShape())
        .invokeWithMetadata(GetAShape(0))
    } yield {
      println(s"prepareShapes: ${r1.value.viable}")
      println(s"getAShape: ${r1.value}")
    }
    
    Await.result(f1, 2.seconds)
    
    val source =
      adornStream(client.getSomeTetrisShapes())
        .invokeWithMetadata(GetSomeTetrisShapes(0,100,250))
    val all_printed = source.runForeach { shape: TetrisShape ⇒
      println(s"Shape: $shape")
    }
    
    all_printed.onComplete {
      case Failure(exception) ⇒
        println(s"GetSomeTetrisShapes failed: ${exception.getMessage}")
        sys.terminate()
      case Success(x) ⇒
        println("Done")
        sys.terminate()
    }
  }
}
