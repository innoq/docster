package services

import play.api.mvc.Result

import scala.concurrent.Future
import scala.util.Try


case object ResultTransformer {

  def transformResult(result: Future[Result]): Try[Future[Result]] = {
    Try(result)
  }

}
