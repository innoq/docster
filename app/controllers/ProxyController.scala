package controllers

import javax.inject.Inject

import play.api._
import play.api.libs.ws._
import play.api.mvc._
import services.ProxyRequestCreator.mapToForwardingRequest
import services.ResultTransformer.transformResult
import services.ServerGateway.forwardRequestToServer

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

import play.api.Logger

class ProxyController @Inject()(ws: WSClient)(configuration: Configuration) extends Controller {

  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext

  def proxy(requestPath: String) = Action.async(parse.tolerantText) { originalRequest =>
    val result: Try[Future[Result]] = for {
      forwardingRequest <- mapToForwardingRequest(originalRequest, requestPath, configuration)
      serverResponse <- Try(forwardRequestToServer(forwardingRequest, ws))
      transformedResult <- transformResult(serverResponse)
    } yield transformedResult
    result match {
      case Failure(ex) =>
        Logger.error(ex.getMessage, ex)
        Future(InternalServerError(ex.getMessage))
      case Success(result) =>
        result
    }
  }

}
