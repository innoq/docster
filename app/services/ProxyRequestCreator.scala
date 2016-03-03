package services

import play.api.Configuration
import play.api.http.MediaRange
import play.api.mvc.Request

import scala.util.{Failure, Success, Try}

case object ProxyRequestCreator {

  val HAL_MIME_TYPE = "application/hal+json"
  val HTML_MIME_TYPE = "text/html"

  def mapToForwardingRequest(request: Request[String], requestPath: String, configuration: Configuration): Try[ProxyRequest] = {
    calculateServerUri(requestPath, configuration).map { uri =>
      val proxyRequest = ProxyRequest(request.method, uri, request.headers.toMap, request.body)
      addJsonHypermediaContentTypes(proxyRequest)
    }
  }

  private def calculateServerUri(path: String, configuration: Configuration): Try[String] = {
    configuration.getString("server.uri") match {
      case Some("unset") => Failure(new IllegalStateException("server base uri not configured"))
      case Some(basePath) => Success(basePath + "/" + path)
      case None => Failure(new IllegalStateException("server base uri not configured"))
    }
  }


  def addJsonHypermediaContentTypes(request: ProxyRequest): ProxyRequest = {
    val enrichedAcceptHeader: Seq[String] = enrichAcceptHeader(request.headers.get("Accept"), List(HAL_MIME_TYPE))
    request.putHeader("Accept", enrichedAcceptHeader)
  }

  def enrichAcceptHeader(acceptHeader: Option[Seq[String]], jsonHypermediaTypes: List[String]): Seq[String] = {

    def hasHtmlHigherPriority(mediaRanges: Seq[MediaRange], jsonTypes: List[String]): Boolean = {
      if (mediaRanges.isEmpty) {
        return true
      }
      val htmlIndex: Int = mediaRanges.indexWhere(_.accepts(HTML_MIME_TYPE))
      val jsonIndex = mediaRanges.indexWhere(_.accepts(HAL_MIME_TYPE))
      (htmlIndex, jsonIndex) match {
        case (-1, -1) => false
        case (-1, acceptsJson) => false
        case (acceptsHtml, -1) => true
        case (acceptsHtml, acceptsJson) => acceptsHtml <= acceptsJson
      }
    }

    def toSingleStringValue: Seq[String] => String = {
      _.mkString(",")
    }

    def toMediaRanges: String => Seq[MediaRange] = {
      MediaRange.parse.apply
    }

    def shouldAddJsonHypermediaTypes(acceptHeader: Seq[String]): Boolean = {
      val jsonTypes = List(HAL_MIME_TYPE)
      val mediaTypeHeader = toSingleStringValue.andThen(toMediaRanges)(acceptHeader)
      hasHtmlHigherPriority(mediaTypeHeader, jsonTypes)
    }

    acceptHeader match {
      case None => List(HTML_MIME_TYPE, HAL_MIME_TYPE + ";q=0.9")
      case Some(headers) if shouldAddJsonHypermediaTypes(headers) => headers :+ HAL_MIME_TYPE + ";q=0.9"
      case Some(headers) => headers
    }
  }

}