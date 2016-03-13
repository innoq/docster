package services

import play.api.libs.iteratee.Enumerator
import play.api.libs.ws.{WSClient, WSResponseHeaders}
import play.api.mvc.Result
import play.api.mvc.Results.Status

import scala.concurrent.Future

case object ServerGateway {

  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext

  def forwardRequestToServer(request: ProxyRequest, ws: WSClient): Future[Result] = {
    val wsRequest = ws.url(request.uri.toString)
      .withHeaders(request.simpleHeaderMap.toList: _*)
      .withMethod(request.method)
      .withBody(request.body)
      .withRequestTimeout(1000)

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
