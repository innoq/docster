package services

import java.net.URI
import javax.inject.{Inject, Singleton}

import play.api.Configuration

@Singleton
class DocsterDB @Inject()(configuration: Configuration) {

  var serverBaseUri: Option[URI] = configuration.getString("server.uri") match {
    case Some("unset") => None
    case Some(value) => Some(URI.create(value))
    case None => None
  }

}
