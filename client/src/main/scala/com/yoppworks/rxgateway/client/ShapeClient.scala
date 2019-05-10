package com.yoppworks.rxgateway.client

import akka.grpc.GrpcClientSettings
import akka.grpc.scaladsl.{SingleResponseRequestBuilder, StreamResponseRequestBuilder}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import com.yoppworks.rxgateway.api._

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object ShapeClient {
  implicit val sys : ActorSystem = ActorSystem("ShapeClient")
  
  // Main program
  def main(args: Array[String]) : Unit = {
    // Boot akka
    implicit val mat: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContextExecutor = sys.dispatcher
  
    // Take details how to connect to the service from the config.
    val clientSettings =
      GrpcClientSettings
        .connectToServiceAt(host = "localhost", port = 8080)
        .withConnectionAttempts(3)
        .withTls(enabled = false)
  
    // Create a client-side stub for the service
    val client: ShapeServiceClient = ShapeServiceClient(clientSettings)
  
    val name = "scala-client"
  
    def adornSingle[R, T](srb : SingleResponseRequestBuilder[R, T]): SingleResponseRequestBuilder[R, T]  =
      srb.addHeader("X-USERNAME", name)
  
    def adornStream[R, T](srb : StreamResponseRequestBuilder[R, T]): StreamResponseRequestBuilder[R, T] =
      srb.addHeader("X-USERNAME", name)
  
    val computation =
      for {
        r1 <- adornSingle(client.prepareShapes()).invokeWithMetadata(PrepareShapes(numberOfShapesToPrepare = 10))
        r2 ← adornSingle(client.getAShape()).invokeWithMetadata(GetAShape())
        r3 <-
          adornStream(client.getSomeTetrisShapes())
            .invokeWithMetadata(GetSomeTetrisShapes(0, 10, 200))
            .runForeach { shape: TetrisShape ⇒
              println(s"Shape: $shape")
              System.out.flush()
            }
        r4 ← adornSingle(client.releaseShapes()).invoke(ReleaseShapes())
      } yield {
        println(s"prepareShapes: ${r1.value}")
        println(s"getAShape    : ${r2.value}")
        println(s"getSomeShapes: $r3")
        println(s"releaseShapes: $r4")
      }
    
    computation.onComplete {
      case Failure(exception) ⇒
        println(s"GetSomeTetrisShapes failed: ${exception.getMessage}")
        sys.terminate()

      case Success(x) ⇒
        println("Done")
        sys.terminate()
    }
  }
}
