package integration

import java.util.concurrent.TimeUnit

import play.api.mvc.{Result, AnyContentAsEmpty}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, Play}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

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

  def call(fakeRequest: FakeRequest[AnyContentAsEmpty.type], application: Application): Option[Result] = {
    route(application, fakeRequest) match {
      case Some(result) => Some(Await.result(result, Duration(1000, TimeUnit.SECONDS)))
      case None => None
    }
  }

  def callWithBody(fakeRequest: FakeRequest[String], application: Application): Option[Result] = {
    route(application, fakeRequest) match {
      case Some(result) => Some(Await.result(result, Duration(1000, TimeUnit.SECONDS)))
      case None => None
    }
  }

}
