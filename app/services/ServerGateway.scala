package services

import javax.inject.Inject

import play.api.Configuration
import play.api.libs.iteratee.Enumerator
import play.api.libs.ws.WSClient
import play.api.mvc.Results.Status
import play.api.mvc.{ResponseHeader, Result}

import scala.concurrent.Future

object ServerGateway

class ServerGateway @Inject()(configuration: Configuration) {

  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext

  def forwardRequestToServer(request: ProxyRequest, ws: WSClient): Future[Result] = {
    val wsRequest = ws.url(request.uri.toString)
      .withHeaders(request.simpleHeaderMap.toList: _*)
      .withMethod(request.method)
      .withBody(request.body.getOrElse(""))
      .withRequestTimeout(configuration.getLong("server.timeout").getOrElse(60000))

    wsRequest.execute().map { response =>
      def responseHeader = toSimpleHeaderMap(response.allHeaders)
      responseHeader.get("Transfer-Encoding") match {
        case Some("chunked") =>
          new Status(response.status)
            .chunked(Enumerator(response.bodyAsBytes))
            .withHeaders(responseHeader.toList: _*)
        case default =>
          val headers =
            responseHeader map {
              // fix the "Content-Lenght" header manually
              h => (h._1, if (h._1 == "Content-Length")
                response.body.toString
              else h._2.head)
            }
          Result(ResponseHeader(response.status, responseHeader), Enumerator(response.bodyAsBytes))

      }


    }

  }

  private def toSimpleHeaderMap(headers: Map[String, Seq[String]]): Map[String, String] = {
    headers.map {
      case (k, v) => (k, v.mkString(","))
    }
  }

}
