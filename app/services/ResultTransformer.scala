package services

import formats.hal.HalTransformer
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

  def transformResult(result: Future[Result], proxyRequest: ProxyRequest, transformers: List[ContentTypeTransformer] = defaultTransformers): Future[Result] = {

    def findTransformerForContentType(result: Result): Option[ContentTypeTransformer] = {
      for {
        mediaRange <- result.header.headers.get("Content-Type").map(MediaRanges.toMediaRange)
        transformer <- transformers.find(transformer => mediaRange.accepts(transformer.from))
      } yield transformer
    }

    def toResult(representation: Representation, status: Int): Result = {
      Results.Ok(views.html.representationPage(representation))
    }

    result.map { (result: Result) =>
      if (proxyRequest.mimeTypeHasHigherOrEqualPrecedence("text/html", transformers.map(_.from))) {
        val transformer = findTransformerForContentType(result)
        val representation = transformer.map(_.transform(proxyRequest, ProxyResponse(result)))
        representation.map(toResult(_, result.header.status)).getOrElse(result)
      } else {
        result
      }

    }
  }

}
