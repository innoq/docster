package controllers

import javax.inject.Inject

import play.api.Logger
import play.api.libs.ws._
import play.api.mvc._
import services.ProxyRequestCreator.mapToForwardingRequest
import services.ResultTransformer.transformResult
import services.{DocsterDB, ServerBaseUriNotConfigured, ServerGateway}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class ProxyController @Inject()(ws: WSClient)(configStore: DocsterDB, serverGateway: ServerGateway) extends Controller {

  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext

  def proxy(requestPath: String) = Action.async(parse.tolerantText) { originalRequest =>
    val result: Try[Future[Result]] = for {
      forwardingRequest <- mapToForwardingRequest(originalRequest, requestPath, configStore)
      serverResponse <- Try(serverGateway.forwardRequestToServer(forwardingRequest, ws))
      transformedResult <- Try(transformResult(serverResponse, forwardingRequest))
    } yield transformedResult

    result match {
      case Failure(ex) =>
        ex match {
          case ex: ServerBaseUriNotConfigured =>
            Future.successful(Redirect(controllers.routes.AdminController.adminConsole()))
          case default =>
            Logger.error(ex.getMessage, ex)
            Future(InternalServerError(ex.getMessage))
        }
      case Success(finalResult) => finalResult
    }
  }
}
