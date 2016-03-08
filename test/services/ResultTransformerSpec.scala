package services

import org.mockito.Mockito._
import org.scalatest.FlatSpec
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Minutes, Span}
import org.specs2.mock.mockito.MockitoMatchers
import play.api.mvc.Results
import play.api.test.Helpers
import play.api.test.Helpers.defaultAwaitTimeout
import services.ResultTransformer._

import scala.concurrent.Future

class ResultTransformerSpec extends FlatSpec with ScalaFutures {

  val ANY = "anything"

  implicit val defaultPatience = PatienceConfig(timeout = Span(20, Minutes), interval = Span(5, Millis))

  behavior of "a result transformer"

  def accept[T](value: T): (String, T) = ("Accept", value)

  it should "return the given result if client not primarily aksed for html" in {

    val result = Results.Ok
    val request = ProxyRequest(uri = "naything").putHeader(accept(List("application/hal+json", "text/html;p=0.9")))

    val transformedResult = transformResult(Future.successful(result), request, Map.empty)

    assert(transformedResult.futureValue === result)
  }

  it should "return the given result if client asked primarily for html but a not known json type was returned" in {

    val result = Results.Ok.withHeaders(accept("application/innoq+json"))
    val request = ProxyRequest(uri = "naything").putHeader(accept(List("application/hal+json;p=0.9", "text/html")))

    val transformedResult = transformResult(Future.successful(result), request, Map.empty)

    assert(transformedResult.futureValue === result)
  }

  it should "call the right transformer and return a documentation if client asked primarily for html but a known json format was returned" in {

    val halJson = "application/hal+json"
    val result = Results.Ok("{}").withHeaders(("Content-Type", halJson))
    val request = ProxyRequest(uri = "naything").putHeader(accept(List(halJson + ";q=0.9", "text/html")))

    val halTransformer = mock(classOf[Transformer])
    val sirenTransformer = mock(classOf[Transformer])

    val documentation = Documentation(ANY, Overview(ANY), List(Relation(ANY, ANY)))
    when(halTransformer.transform(MockitoMatchers.any, MockitoMatchers.any)).thenReturn(documentation)

    val transformedResult = transformResult(Future.successful(result), request, Map((halJson, halTransformer), ("application/vnd.siren+json", sirenTransformer)))

    assert(Helpers.status(transformedResult) == 200)
    verify(halTransformer).transform(MockitoMatchers.any, MockitoMatchers.any)
  }


}
