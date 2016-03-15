package services

import java.util.concurrent.TimeUnit
import javax.inject.Inject

import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.ws.{WSClient, WSResponseHeaders}
import play.api.mvc.Result
import play.api.mvc.Results.Status
import play.api.{Configuration, mvc}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object ServerGateway

class ServerGateway @Inject()(configuration: Configuration) {

  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext

  def getBody(body: Enumerator[Array[Byte]], timeout: Long): Array[Byte] =
    Await.result(body |>>> Iteratee.consume[Array[Byte]](), Duration(timeout, TimeUnit.MILLISECONDS))


  def forwardRequestToServer(request: ProxyRequest, ws: WSClient): Future[Result] = {
    val wsRequest = ws.url(request.uri.toString)
      .withHeaders(request.simpleHeaderMap.toList: _*)
      .withMethod(request.method)
      .withBody(request.body.getOrElse(""))
      .withRequestTimeout(configuration.getLong("server.timeout").getOrElse(60000))

    wsRequest.stream().map {
      case (headers: WSResponseHeaders, body: Enumerator[Array[Byte]]) =>
        val responseHeader: Map[String, String] = toSimpleHeaderMap(headers.headers)
        responseHeader.get("Transfer-Encoding") match {
          case Some("chunked") =>
            new Status(headers.status)
              .chunked(body)
              .withHeaders(responseHeader.toList: _*)
          case default =>
            val retrievedBody = getBody(body, configuration.getLong("server.timeout").getOrElse(60000))
//            Result(ResponseHeader(headers.status, responseHeader), body)
            mvc.Results.Ok(new String(retrievedBody)).withHeaders(responseHeader.toList: _*)
            
        }


    }

  }

  private def toSimpleHeaderMap(headers: Map[String, Seq[String]]): Map[String, String] = {
    headers.map {
      case (k, v) => (k, v.mkString(","))
    }
  }

}
