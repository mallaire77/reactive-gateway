package com.yoppworks.rxgateway.server.lib

import akka.actor.ActorSystem
import akka.grpc.Trailers
import akka.grpc.scaladsl.GrpcExceptionHandler
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer

import com.yoppworks.rxgateway.utils.ChainingSyntax

import scala.concurrent.{ExecutionContext, Future}

trait GrpcServer extends ChainingSyntax {
  implicit def system: ActorSystem

  implicit def materializer: ActorMaterializer

  implicit def ec: ExecutionContext

  def name: String

  def interface: String

  def port: Int

  final type SystemRejectionHandler =
    ActorSystem => PartialFunction[Throwable, Trailers]
  
  def GrpcHandler: SystemRejectionHandler => HttpRequest => Future[HttpResponse]

  private lazy val innerHandler =
    GrpcHandler { system: ActorSystem =>
      GrpcRejectionHandler.errorMapper(system)
        .orElse(GrpcExceptionHandler.defaultMapper(system))
    }

  // Bind service handler servers to configured values
  private lazy val binding =
    Http().newServerAt(
      interface = interface,
      port = port,
    ).bind(handler)

  private def mapRequest(route: HttpRequest => Future[HttpResponse]): HttpRequest => Future[HttpResponse] =
    request =>
      (RawHeader("grpc-accept-encoding", "identity") +: request.headers.filter(x => x.name != "grpc-accept-encoding"))
        .pipe { newHeaders =>
          route(request.withHeaders(headers = newHeaders))
        }

  private def logRequest(route: HttpRequest => Future[HttpResponse]): HttpRequest => Future[HttpResponse] =
    request =>
      System.currentTimeMillis.pipe { start =>
        route(request).map { response =>
          system.log.info {
            s"request_uri=${request.uri} " +
              s"request_method=${request.method.value} " +
              s"request_headers=${request.headers.mkString("(", ", ", ")")} " +
              s"""response_status="${response.status}" """ +
              s"""response_type="${response.entity.contentType}" """ +
              s"response_headers=${response.headers.mkString("(", ", ", ")")} " +
              s"epoch=${System.currentTimeMillis - start}"
          }

          response
        }
      }

  private def handler: HttpRequest => Future[HttpResponse] =
    mapRequest {
      logRequest { request =>
        innerHandler(request)
      }
    }

  def run()(implicit system: ActorSystem): Unit = {
    // Report successful binding
    binding.foreach { binding =>
      system.log.info(s"gRPC over HTTP/2 server bound to: ${binding.localAddress}")
    }
  }
}
