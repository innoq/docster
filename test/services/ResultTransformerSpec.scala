package services

import java.net.URI

import model.{Relation, Representation}
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

  val ANY_URI = URI.create("http://example.com")

  implicit val defaultPatience = PatienceConfig(timeout = Span(20, Minutes), interval = Span(5, Millis))

  behavior of "a result transformer"

  def accept[T](value: T): (String, T) = ("Accept", value)

  it should "return the given result if client not primarily aksed for html" in {

    val result = Results.Ok
    val request = ProxyRequest(uri = ANY_URI).putHeader(accept(List("application/hal+json", "text/html;p=0.9")))

    val transformedResult = transformResult(Future.successful(result), request, List.empty)

    assert(transformedResult.futureValue === result)
  }

  it should "return the given result if client asked primarily for html but a not known json type was returned" in {

    val result = Results.Ok.withHeaders(accept("application/innoq+json"))
    val request = ProxyRequest(uri = ANY_URI).putHeader(accept(List("application/hal+json;p=0.9", "text/html")))

    val transformedResult = transformResult(Future.successful(result), request, List.empty)

    assert(transformedResult.futureValue === result)
  }

  it should "call the right transformer and return a documentation if client asked primarily for html but a known json format was returned" in {

    val halJson = "application/hal+json"
    val result = Results.Ok("{}").withHeaders(("Content-Type", halJson))
    val request = ProxyRequest(uri = ANY_URI).putHeader(accept(List(halJson + ";q=0.9", "text/html")))

    val halTransformer = mock(classOf[ContentTypeTransformer])
    when(halTransformer.from).thenReturn("application/hal+json")
    val sirenTransformer = mock(classOf[ContentTypeTransformer])
    when(sirenTransformer.from).thenReturn("application/vnd.siren+json")

    val representation = Representation(ANY, relations = List.empty)
    when(halTransformer.transform(MockitoMatchers.any, MockitoMatchers.any)).thenReturn(representation)

    val transformedResult = transformResult(Future.successful(result), request, List(halTransformer, sirenTransformer))

    assert(Helpers.status(transformedResult) == 200)
    verify(halTransformer).transform(MockitoMatchers.any, MockitoMatchers.any)
  }

  it should "transform absolute uris to the target host to relative ones" in {

    val relations = List(Relation("relation1", "https://myhost/orders/0"))

    val result = transformUris(Representation("anyName", relations = relations), "myhost")

    assert(result.relations.head.uri == "/orders/0")
  }


  it should "transform absolute uris to the target host to relative ones also for embedded representations" in {

    val embeddedRepresentation = Representation("anyName", relations = List(Relation("relation1", "https://myhost/orders")))

    val result = transformUris(Representation(ANY, embeddedRepresentations = Map((ANY, List(embeddedRepresentation)))), "myhost")

    assert(result.embeddedRepresentations.head._2.head.relations.head.uri == "/orders")
  }

  it should "not transform absolute uris to a different host" in {

    val relations = List(Relation("relationToDifferentHost", "http://otherHost/orders"))

    val result = transformUris(Representation("anyName", relations = relations), "http://myhost")

    assert(result.relations.head.uri == "http://otherHost/orders")
  }

}
