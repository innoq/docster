package services

import javax.inject.Inject

import play.api.Configuration
import play.api.libs.iteratee.Enumerator
import play.api.libs.ws.WSClient
import play.api.mvc.{ResponseHeader, Result}

import scala.concurrent.Future

object ServerGateway

class ServerGateway @Inject()(configuration: Configuration) {

  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext

  def forwardRequestToServer(request: ProxyRequest, ws: WSClient): Future[Result] = {
    ws.url(request.uri.toString)
    .withMethod(request.method)
    .withHeaders(toSimpleHeaderMap(request.headers).toList: _*)
    .withBody(request.body.getOrElse("")) // Content-Length header will be updated automatically
    .execute()
      .map {
        response => {
          val body =
            response.body
          val headers =
            response.allHeaders map {
              // fix the "Content-Lenght" header manually
              h => (h._1, if (h._1 == "Content-Length")
                body.length.toString
              else h._2.head)
            }
          Result(ResponseHeader(response.status, headers), Enumerator(body.getBytes))
        }
      }

  }

  private def toSimpleHeaderMap(headers: Map[String, Seq[String]]): Map[String, String] = {
    headers.map {
      case (k, v) => (k, v.mkString(","))
    }
  }

}
