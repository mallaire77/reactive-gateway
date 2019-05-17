package com.yoppworks.rxgateway.server.lib

import akka.actor.ActorSystem
import akka.grpc.scaladsl.GrpcExceptionHandler
import akka.http.scaladsl.{Http, HttpConnectionContext}
import akka.http.scaladsl.UseHttp2.Always
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.Uri.Path.Segment
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer

import com.yoppworks.rxgateway.utils.ChainingSyntax

import io.grpc.Status

import scala.concurrent.{ExecutionContext, Future}

trait GrpcServer extends ChainingSyntax {
  implicit def system: ActorSystem

  implicit def materializer: ActorMaterializer

  implicit def ec: ExecutionContext

  def name: String

  def interface: String

  def port: Int
  
  final type SystemRejectionHandler =
    ActorSystem => PartialFunction[Throwable, Status]
  
  def GrpcHandler: SystemRejectionHandler => HttpRequest => Future[HttpResponse]

  def GrpcRejectionHandlers: SystemRejectionHandler

  private lazy val innerHandler =
    GrpcHandler { system: ActorSystem =>
      GrpcRejectionHandlers(system)
        .orElse(GrpcRejectionHandler.errorMapper(system))
        .orElse(GrpcExceptionHandler.defaultMapper(system))
    }

  // Bind service handler servers to configured values
  private lazy val binding =
    Http().bindAndHandleAsync(
      handle,
      interface = interface,
      port = port,
      connectionContext = HttpConnectionContext(http2 = Always))

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

  private def handle: HttpRequest => Future[HttpResponse] =
    logRequest { request =>
      request.uri.path match {
        case Path.Slash(Path.Empty) | Path.Empty =>
          Future.successful(HttpResponse(entity = HttpEntity("Akka Grpc w/ HTTP2 Demo")))

        case Path.Slash(Segment("ping", Path.Empty)) =>
          Future.successful(HttpResponse(entity = HttpEntity("Pong!")))

        case _ =>
          innerHandler(request)
      }
    }

  def run()(implicit system: ActorSystem): Unit = {
    // Report successful binding
    binding.foreach {binding =>
      system.log.info(s"gRPC over HTTP/2 server bound to: ${binding.localAddress}")
    }
  }
}
