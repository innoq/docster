package integration.helper

import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.Helpers._
import play.api.test.{FakeApplication, FakeRequest}
import play.api.{Application, Play}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

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


  def application(host: String = "localhost", port: Option[Integer] = None): FakeApplication = {
    val portPart= port.map( port => s":$port").getOrElse("")
    FakeApplication(additionalConfiguration = Map(("server.uri", s"http://$host$portPart")))
  }

}
