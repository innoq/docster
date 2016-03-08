package services

import java.util.concurrent.TimeUnit

import play.api.http.HeaderNames
import play.api.libs.iteratee.Iteratee
import play.api.mvc.{Result, Results}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object ProxyResponse {

  def apply(result: Result): ProxyResponse = {

    val body = new String(contentAsBytes(result))
    new ProxyResponse(result.header.status, result.header.headers.mapValues(_.split(",").toList), body)
  }

  private def contentAsBytes(result: Result): Array[Byte] = {
    val eBytes = result.header.headers.get(HeaderNames.TRANSFER_ENCODING) match {
      case Some("chunked") => result.body &> Results.dechunk
      case _ => result.body
    }
    Await.result(eBytes |>>> Iteratee.consume[Array[Byte]](), Duration(1, TimeUnit.SECONDS))
  }
}

case class ProxyResponse(status: Int = 200, headers: Map[String, Seq[String]] = Map(), body: String = "")


