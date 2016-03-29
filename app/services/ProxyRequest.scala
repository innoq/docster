package services

import java.net.URI

import play.api.http.MediaRange
import services.MediaRanges._

/**
 * Wrapper for the original client request which should be forwarded to the configured server.
 *
 * Used instead of plays request object to have a more simpler and easier to test value object.
 *
 * @param method HTTP method
 * @param uri target uri
 */
case class ProxyRequest(method: String = "GET", uri: URI,  httpMessage: HttpMessage = HttpMessage(Map.empty, None)) {

  lazy val mediaRanges = httpMessage.headers.get("Accept").map(toMediaRanges).getOrElse(List.empty).reverse

  def simpleHeaderMap: Map[String, String] = {
    httpMessage.headers.map {
      case (k, v) => (k, v.mkString(","))
    }
  }

  def putHeader(key: String, value: Seq[String]): ProxyRequest = {
    copy(httpMessage = HttpMessage(httpMessage.headers.updated(key, value), httpMessage.body))
  }

  def putHeader(header: (String, Seq[String])): ProxyRequest = {
    putHeader(header._1, header._2)
  }

  private def mimeTypeIndex(s: String, ranges: Seq[MediaRange] = mediaRanges) = {
    ranges.indexWhere(_.accepts(s))
  }

  def compareMimeTypePrecedence(a: String, b: String): Int = {
    mimeTypeIndex(a).compareTo(mimeTypeIndex(b))
  }

  def mimeTypeHasHigherOrEqualPrecedence(single: String, others: Iterable[String]): Boolean = {
    def max: Int = {
      val indices = others.map(mimeTypeIndex(_, mediaRanges))
      if (indices.isEmpty)
        -1
      else
        indices.max
    }

    val bestIndexOthers = max
    val bestIndexSingle = mimeTypeIndex(single, mediaRanges)

    bestIndexSingle >= bestIndexOthers
  }

}

