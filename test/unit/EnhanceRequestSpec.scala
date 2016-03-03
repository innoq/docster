package unit

import org.scalatest.FlatSpec
import services.ProxyRequestCreator.enrichAcceptHeader

class EnhanceRequestSpec extends FlatSpec {

  val jsonHypermediaTypes = List("application/hal+json")

  behavior of "request forwarded to the server"

  it should "not be changed if it already asks primarily for a known json hypermedia type" in {
    val acceptHeaderValues = Some(List("application/hal+json", "text/html;q=0.8"))

    val result = enrichAcceptHeader(acceptHeaderValues, jsonHypermediaTypes)

    assert(result == acceptHeaderValues.get)
  }

  it should "add all known json hypermedia types to the accept header if request asks for html" in {
    val acceptHeaderValues = Some(List("text/html"))

    val result = enrichAcceptHeader(acceptHeaderValues, jsonHypermediaTypes)

    assert(result == List("text/html", "application/hal+json;q=0.9"))
  }

  it should "treat a missing Accept header as if html was requested" in {
    val result = enrichAcceptHeader(None, jsonHypermediaTypes)

    assert(result == List("text/html", "application/hal+json;q=0.9"))
  }

  it should "add json hypermedia types even if accept header contains */*" in {
    val acceptHeaderValues = Some(List("text/html", "*/*"))

    val result = enrichAcceptHeader(acceptHeaderValues, jsonHypermediaTypes)

    assert(result == acceptHeaderValues.get :+ "application/hal+json;q=0.9")
  }

  it should "add hypermedia types to default Chrome accept header" in {

    val acceptHeaderValues = Some(List("text/html", "application/xhtml+xml", "application/xml;q=0.9", "image/webp", "*/*;q=0.8"))

    val result = enrichAcceptHeader(acceptHeaderValues, jsonHypermediaTypes)

    assert(result.exists(_.contains("application/hal+json")))
  }

  it should "add hypermedia types to default Safari accept header" in {

    val acceptHeaderValues = Some(List("text/html", "application/xhtml+xml", "application/xml;q=0.9", "*/*;q=0.8"))

    val result = enrichAcceptHeader(acceptHeaderValues, jsonHypermediaTypes)

    assert(result.exists(_.contains("application/hal+json")))
  }

  it should "add hypermedia types to default Internet Explorer accept header" in {

    // holy crap...
    val acceptHeaderValues = Some(List("image/gif", "image/jpeg", "image/pjpeg", "application/x-ms-application", "application/vnd.ms-xpsdocument", "application/xaml+xml", "application/x-ms-xbap", "application/x-shockwave-flash", "application/x-silverlight-2-b2", "application/x-silverlight", "application/vnd.ms-excel", "application/vnd.ms-powerpoint", "application/msword", "*/*"))

    val result = enrichAcceptHeader(acceptHeaderValues, jsonHypermediaTypes)

    assert(result.exists(_.contains("application/hal+json")))
  }


}

