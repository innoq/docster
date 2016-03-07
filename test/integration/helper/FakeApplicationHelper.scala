package integration.helper

import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.Helpers._
import play.api.test.{FakeApplication, FakeRequest}
import play.api.{Application, Play}

import scala.concurrent.Await
import scala.concurrent.duration._

object FakeApplicationHelper {


  def withApplication(app: Application)(call: () => Unit): Unit = {
    Play.start(app)
    try {
      call()
    }
    finally {
      Play.stop(app)
    }
  }

  def call(fakeRequest: FakeRequest[AnyContentAsEmpty.type], application: Application): Option[Result] =
    route(application, fakeRequest).map(t => Await.result(t, 100000 seconds))


  def callWithBody(fakeRequest: FakeRequest[String], application: Application): Option[Result] =
    route(application, fakeRequest).map(t => Await.result(t, 100000 seconds))


  def application(port: Integer): FakeApplication = {
    FakeApplication(additionalConfiguration = configurationWithWireMockBaseUri(port))
  }

  def configurationWithWireMockBaseUri(port: Integer): Map[String, String] = {
    Map(("server.uri", "http://localhost:" + port))
  }

}
