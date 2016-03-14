package services

import javax.inject.Inject

import play.api.Configuration
import play.api.libs.iteratee.Enumerator
import play.api.libs.ws.{WSResponseHeaders, WSClient}
import play.api.mvc.Result
import play.api.mvc.Results.Status

import scala.concurrent.Future

object ServerGateway

class ServerGateway @Inject()(configuration: Configuration) {

  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext

  def forwardRequestToServer(request: ProxyRequest, ws: WSClient): Future[Result] = {
    val wsRequest = ws.url(request.uri.toString)
      .withHeaders(request.simpleHeaderMap.toList: _*)
      .withMethod(request.method)
      .withBody(request.body)
      .withRequestTimeout(configuration.getLong("server.timeout").getOrElse(60000))

    wsRequest.stream().map {
      case (headers: WSResponseHeaders, body: Enumerator[Array[Byte]]) =>
        new Status(headers.status)
          .chunked(body)
          .withHeaders(toSimpleHeaderMap(headers.headers).toList: _*)
    }

  }

  private def toSimpleHeaderMap(headers: Map[String, Seq[String]]): Map[String, String] = {
    headers.map {
      case (k, v) => (k, v.mkString(","))
    }
  }

}
