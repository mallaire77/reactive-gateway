package com.yoppworks.rxgateway.server

import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.Uri.Path.Segment
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse}

import scala.concurrent.Future

case class API(default: HttpRequest => Future[HttpResponse]) {
  def apply(request: HttpRequest): Future[HttpResponse] = {
    println(s"uri=${request.uri.toString()}")

    request.uri.path match {
      case Path.Slash(Segment("ping", Path.Empty)) =>
        Future.successful(HttpResponse(entity = HttpEntity("Pong!")))

      case _ =>
        default(request)
    }
  }
}
