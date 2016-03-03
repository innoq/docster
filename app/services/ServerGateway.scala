package services

import play.api.libs.ws.WSClient
import play.api.mvc.{ResponseHeader, Result}

import scala.concurrent.Future

case object ServerGateway {

  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext


  def forwardRequestToServer(request: ProxyRequest, ws: WSClient): Future[Result] = {
    val wsRequest = ws.url(request.uri)
      .withHeaders(request.simpleHeaderMap.toList: _*)
      .withMethod(request.method)
      .withBody(request.body)
      .withRequestTimeout(1000)

    wsRequest.stream().map {
      case (response, body) =>
        Result(ResponseHeader(response.status, toSimpleHeaderMap(response.headers)), body)
    }
  }

  private def toSimpleHeaderMap(headers: Map[String, Seq[String]]): Map[String, String] = {
    headers.map {
      case (k, v) => (k, v.mkString(","))
    }
  }

}
