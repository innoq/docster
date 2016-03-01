package controllers

import javax.inject.Inject

import play.api.Play.current
import play.api._
import play.api.libs.ws.{ InMemoryBody, WSClient, WSRequest, WSResponseHeaders }
import play.api.mvc._

import scala.concurrent.Future

class ProxyController @Inject() (ws: WSClient) (configuration: Configuration) extends Controller {

  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext

  def proxy(requestPath: String) = Action.async(parse.tolerantText) { originalRequest =>
    calculateServerUri(requestPath) match {
      case Some(serverUri) =>
        forwardRequestToServer(originalRequest, serverUri )
      case None => Future(InternalServerError)
    }
  }

  private def calculateServerUri(path: String): Option[String] = {
    configuration.getString("server.uri") match {
      case Some("unset") => None
      case Some(basePath) => Some(basePath + "/" + path)
      case None => None
    }
  }

  private def forwardRequestToServer(originalRequest: Request[String], serverUri: String): Future[Result] = {
    val serverWsRequest = toServerRequest(originalRequest, serverUri)
    serverWsRequest.stream().map {
      case (response, body) =>
        Result(ResponseHeader(response.status, toSimpleHeaderMap(response)), body)
      case noResponse => InternalServerError
    }
  }

  private def toServerRequest(request: Request[String], uri: String): WSRequest = {
    val result = ws.url(uri)
      .withMethod(request.method)
      .withHeaders(request.headers.toSimpleMap.toList: _*)
      .withBody(InMemoryBody(request.body.getBytes))
    result
  }

  private def toSimpleHeaderMap(response: WSResponseHeaders): Map[String, String] = {
    response.headers.map {
      case (k, v) => (k, v.head)
    }
  }
}
