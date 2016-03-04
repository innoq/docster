package services

import play.api.mvc.Result

import scala.concurrent.Future

trait Transformer {

  def transform(serverResult: Future[Result]): Future[Documentation]
}
