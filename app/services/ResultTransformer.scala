package services

import hal.HalTransformer
import play.api.mvc.Result

import scala.concurrent.Future


case object ResultTransformer {

  def transformers = Map[String, Transformer]{("application/hal+json", HalTransformer)}

  def transformResult(result: Future[Result], proxyRequest: ProxyRequest, transformers: Map[String, Transformer] = transformers): Either[Future[Result], Documentation] = {
    Left(result)
  }

}
