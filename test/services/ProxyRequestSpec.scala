package services

import java.net.URI

import org.scalatest.FlatSpec

class ProxyRequestSpec extends FlatSpec {

  def accept[T](t: T) = ("Accept", t)

  def ANY_URI = URI.create("http://example.com")

  behavior of "a ProxyRequest"

  it should "have a mediaRange seq (representing the accept header) that is ordered from the lowest prio up to the highest" in {

    val request = ProxyRequest(uri = ANY_URI, httpMessage = HttpMessage(Map(
      accept(List(
        "text/xml;q=0.8",
        "text/html",
        "application/hal+json;q=0.6",
        "*/*;q=0.1"))),
      None))

    assert(request.mediaRanges.map(_.toString()) == List("*/*; q=0.1", "application/hal+json; q=0.6", "text/xml; q=0.8", "text/html"))
  }

  it should "correctly compute if a given mime type has higher precedence than all other given mime types if type is directly given" in {

    val request = ProxyRequest(uri = ANY_URI, httpMessage =
      HttpMessage(Map(
        accept(List(
          "application/hal+json;q=0.9",
          "text/html"))),
        None))

    val result = request.mimeTypeHasHigherOrEqualPrecedence("text/html", List("application/hal+json"))

    assert(result)
  }

  it should "correctly compute if a given mime type has higher precedence than all other given mime types if type is matched by */*" in {

    val request = ProxyRequest(uri = ANY_URI, httpMessage = HttpMessage(Map(
      accept(List(
        "application/hal+json;q=0.9", "*/*"))),
      None))

    val result = request.mimeTypeHasHigherOrEqualPrecedence("text/html", List("application/hal+json"))

    assert(result)
  }

  it should "correctly compute if a given mime type has higher precedence than all other given mime types if one of the other types is unknown" in {

    val request = ProxyRequest(uri = ANY_URI, httpMessage =
      HttpMessage(Map(
        accept(List(
          "application/hal+json;q=0.9", "text/html"))),
        None))

    val result = request.mimeTypeHasHigherOrEqualPrecedence("text/html", List("application/hal+json, application/vnd+siren"))

    assert(result)
  }

}
