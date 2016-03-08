package integration.proxy

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock._
import integration.helper.FakeApplicationHelper._
import integration.helper.WireMockHelper._
import integration.helper.{FakeApplicationHelper, WireMockHelper}
import org.scalatest.FlatSpec
import play.api.test._

class TransparentProxyIntegrationSpec extends FlatSpec {

  val missingConfiguration: Map[String, Nothing] = Map()

  behavior of "docster as a transparent proxy"

  it should "return a 500 if server base uri is not configured" in {

    val app = FakeApplication(additionalConfiguration = Map())

    withApplication(app) { () =>
      val result = call(FakeRequest("GET", randomUri), app)
      assert(result.isDefined && result.get.header.status == 500)
    }
  }

  it should "forward incoming requests for all http methods" in {

    val wireMockUri = randomUri
    val port = freePort()
    val server = new WireMockServer(port)
    val app = application(port = Some(port))

    val methods = List("GET", "PUT", "POST", "DELETE", "HEAD")

    withAppAndMock(app, server, () => {
      methods.foreach { method =>
        server.stubFor(any(urlPathEqualTo(wireMockUri)).willReturn(aResponse().withStatus(200)))
        val request = FakeRequest(method, wireMockUri)

        val result = call(request, app)

        assert(result.isDefined && result.get.header.status == 200, "for request: " + request)
      }
    })
  }

  it should "forward all incoming headers" in {

    val wireMockUri = randomUri
    val port = freePort()
    val server = new WireMockServer(port)
    val app = application(port = Some(port))

    withAppAndMock(app, server, () => {
      val headers = Map(("Content-Type", "text/html"), ("Location", "somewhere"), ("X-Something", "something"))
      val request = FakeRequest("GET", wireMockUri).withHeaders(headers.toList: _*)

      call(request, app)

      assertSameHeader(server, headers)
    })
  }


  it should "forward the incoming body" in {

    val port = freePort()
    val server = new WireMockServer(port)
    val app = application(port = Some(port))

    withAppAndMock(app, server, () => {
      val body = "Hello World"
      val request = FakeRequest("POST", randomUri).withBody(body)

      callWithBody(request, app)

      assert(findCapturedRequest(server).getBodyAsString == body)
    })
  }

  it should "call the same uri on the server" in {

    val wireMockUri = "/this/is/just/a/test"
    val port = freePort()
    val server = new WireMockServer(port)
    val app = application(port = Some(port))

    withAppAndMock(app, server, () => {
      val request = FakeRequest("GET", wireMockUri)

      call(request, app)

      assert(findCapturedRequest(server).getUrl == wireMockUri)
    })
  }

  it should "forward servers response back to the client" in {

    val wireMockUri = randomUri
    val port = freePort()
    val server = new WireMockServer(port)
    val app = application(port = Some(port))

    withAppAndMock(app, server, () => {
      val body = "Hello World"
      val header = ("anyHeader", "anyHeaderValue")
      val mockResponse: ResponseDefinitionBuilder = aResponse()
        .withStatus(200)
        .withBody(body)
        .withHeader(header._1, header._2)
      server.stubFor(any(urlEqualTo(wireMockUri)).willReturn(mockResponse))
      val request = FakeRequest("POST", wireMockUri).withBody(body)

      val proxyResponse = callWithBody(request, app)

      assert(proxyResponse.isDefined && proxyResponse.get.header.status == 200 && proxyResponse.get.header.headers.get(header._1).get == header._2)
    })
  }



  def assertSameHeader(server: WireMockServer, headers: Map[String, String]): Unit = {
    val capturedRequest = findCapturedRequest(server)
    headers.foreach { case (k, v) =>
      assert(capturedRequest.getHeader(k) == v)
    }
  }


}
