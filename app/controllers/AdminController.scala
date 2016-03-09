package controllers

import java.net.URI
import javax.inject.Inject

import play.api.mvc.{Action, Controller}
import services.DocsterConfiguration

import scala.concurrent.Future


class AdminController @Inject()(docsterConfig: DocsterConfiguration) extends Controller {

  def adminConsole = Action.async { implicit request =>
    Future.successful(Ok(views.html.admin(docsterConfig)))
  }

  def configChange = Action.async { request =>
    val body = request.body.asFormUrlEncoded
    val uri = for {
      uriValues <- body.map(_.get("server.uri"))
      uri <- uriValues.flatMap(_.headOption)
    } yield uri

    uri match {
      case Some(value) =>
        docsterConfig.serverBaseUri = Some(URI.create(value))
        Future.successful(Redirect(routes.AdminController.adminConsole()).flashing("success" -> "Config saved!"))
      case None =>
        Future.successful(Redirect(routes.AdminController.adminConsole()).flashing("failure"-> "Missing server uri value"))
    }
  }
}
