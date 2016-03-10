package services

import formats.hal.HalTransformer
import play.api.mvc.{Result, Results}

import scala.concurrent.Future


case object ResultTransformer {

  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext

  def defaultTransformers = Map[String, Transformer]{("application/hal+json", HalTransformer)}

  def transformResult(result: Future[Result], proxyRequest: ProxyRequest, transformers: Map[String, Transformer] = defaultTransformers): Future[Result] = {

    def findTransformerForContentType(result: Result): Option[Transformer] = {
      for {
        mediaRange <- result.header.headers.get("Content-Type").map(MediaRanges.toMediaRange)
        transformerMapping <- transformers.find((pair) => mediaRange.accepts(pair._1))

      } yield transformerMapping._2
    }

    def documentationToResult(representation: Representation, status: Int): Result = {
      Results.Ok(views.html.resource(representation))
    }

    result.map { (result: Result) =>
      if (proxyRequest.mimeTypeHasHigherOrEqualPrecedence("text/html", transformers.keySet)) {
        val transformer = findTransformerForContentType(result)
        val documentationFromResult = transformer.map(_.transform(proxyRequest, ProxyResponse(result)))
        documentationFromResult.map(documentationToResult(_, result.header.status)).getOrElse(result)
      } else {
        result
      }

    }
  }

}
