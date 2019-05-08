package com.yoppworks.rxgateway.server.lib

import akka.actor.ActorSystem
import akka.grpc.scaladsl.GrpcExceptionHandler
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.Uri.Path.Segment
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse}

import com.yoppworks.rxgateway.server.lib.Handler.SystemErrorHandler
import com.yoppworks.rxgateway.utils.ChainingSyntax

import io.grpc.Status

import scala.concurrent.{ExecutionContext, Future}

case class Handler(
  handler: SystemErrorHandler => HttpRequest => Future[HttpResponse]
)(registeredErrorHandlers: SystemErrorHandler)(implicit ec: ExecutionContext) extends ChainingSyntax {
  private val innerHandler =
    handler { system =>
      registeredErrorHandlers(system)
        .orElse(ErrorHandler.errorMapper(system))
        .orElse(GrpcExceptionHandler.defaultMapper(system))
    }

  def apply(request: HttpRequest): Future[HttpResponse] =
    logRequest { request =>
      request.uri.path match {
        case Path.Slash(Segment("ping", Path.Empty)) =>
          Future.successful(HttpResponse(entity = HttpEntity("Pong!")))

        case _ =>
          innerHandler(request)
      }
    }(request)

  private def logRequest(routes: HttpRequest => Future[HttpResponse])(request: HttpRequest): Future[HttpResponse] =
    System.currentTimeMillis.pipe { start =>
      routes(request).map { response =>
        println {
          s"request=$request " +
            s"epoch=${System.currentTimeMillis - start}"
        }

        response
      }
    }
}

object Handler {
  type SystemErrorHandler = ActorSystem => PartialFunction[Throwable, Status]
}
