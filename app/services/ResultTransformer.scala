package services

import com.softwaremill.quicklens._
import formats.hal.HalTransformer
import model.Representation
import play.api.mvc.{Result, Results}

import scala.concurrent.Future

/**
 * Pipeline element which could transform the server result based on the available transformers and the original request.
 * <p>
 * Algorithm:
 * <ul>
 * <li>check if the result should be transformed or not and if a matching transformer is available</li>
 * <li> if yes => transform the result and return it </li>
 * <li> if no => return the given result without modifications </li>
 * </ul>
 */
case object ResultTransformer {

  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext

  def defaultTransformers = List(HalTransformer)

  def transformUris(representation: Representation, host: String): Representation = {

    def updateEmbeddedRepresentations() = representation.embeddedRepresentations.mapValues((representations) => representations.map(transformUris(_, host)))

    val updatedChilds = updateEmbeddedRepresentations()

    representation
      .modify(_.embeddedRepresentations).setTo(updatedChilds)
      .modify(_.relations.each.uri).using(toRelativeUri(_, host))
  }

  def toRelativeUri(uriToTransform: String, host: String): String = {

    def uriPattern = "https?:\\/\\/(.*?:?\\d*)(?:\\/)(.*)".r

    def _toRelativeUri = {
      uriPattern.findFirstMatchIn(uriToTransform).map(_.group(2)).getOrElse(uriToTransform)
    }

    val uriHost = uriPattern.findFirstMatchIn(uriToTransform).map(_.group(1))
    val sameHost = uriHost.contains(host)

    if (sameHost) _toRelativeUri else uriToTransform
  }

  def transformResult(result: Future[Result], proxyRequest: ProxyRequest, transformers: List[ContentTypeTransformer] = defaultTransformers): Future[Result] = {

    def findTransformerForContentType(result: Result): Option[ContentTypeTransformer] = {
      for {
        mediaRange <- result.header.headers.get("Content-Type").map(MediaRanges.toMediaRange)
        transformer <- transformers.find(transformer => mediaRange.accepts(transformer.from))
      } yield transformer
    }

    def toDocumentation(representation: Option[Representation], request: ProxyRequest, response: ProxyResponse): Option[Documentation] = {
      representation
        .map(transformUris(_, proxyRequest.uri.getHost))
        .map(Documentation(_, request))
    }

    def toRepresentation(transformer: Option[ContentTypeTransformer], request: ProxyRequest, response: ProxyResponse): Option[Representation] = {
      transformer.map(_.transform(proxyRequest, response))
    }

    def toResult(documentation: Option[Documentation], request: ProxyRequest, response: ProxyResponse): Option[Result] = {
      documentation.map { documentation =>
        Results.Ok(views.html.documentation(documentation))
      }
    }

    result.map { (result: Result) =>
      if (proxyRequest.mimeTypeHasHigherOrEqualPrecedence("text/html", transformers.map(_.from))) {
        val proxyResponse = ProxyResponse(result)
        val transformer = findTransformerForContentType(result)
        val representation = toRepresentation(transformer, proxyRequest, proxyResponse)
        val documentation = toDocumentation(representation, proxyRequest, proxyResponse)
        val transformedResult = toResult(documentation, proxyRequest, proxyResponse)
        transformedResult.getOrElse(result)
      } else {
        result
      }

    }
  }

}
