package integration.proxy

import java.util.UUID

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import integration.helper.FakeApplicationHelper._
import integration.helper.WireMockHelper._
import integration.helper.{FakeApplicationHelper, WireMockHelper}
import org.scalatest.FlatSpec
import play.api.test.Helpers._
import play.api.test.{FakeApplication, FakeRequest}

class TransformingProxyIntegrationSpec extends FlatSpec {

  behavior of "docster as a transforming proxy"


  it should "return an html representation of the json hypermedia api if the client asks for one but the server only supports a known json content-type" in {

    val wireMockUri = "/" + UUID.randomUUID()
    val port = freePort()
    val server = new WireMockServer(port)
    val app: FakeApplication = application(port)

    withAppAndMock(app, server, () => {

      server.stubFor(get(urlEqualTo(wireMockUri))
        .withHeader("Accept", containing("text/html"))
        .willReturn(aResponse().withStatus(406)))

      server.stubFor(get(urlEqualTo(wireMockUri))
        .withHeader("Accept", containing("application/hal+json"))
        .willReturn(aResponse()
          .withStatus(200)
          .withBody("{}")
          .withHeader("Content-Type", "application/hal+json")
          .withHeader("content-length", "2")))

      val request = FakeRequest("GET", wireMockUri).withHeaders(("Accept", "text/html"))

      val result = route(app, request).get

      assert(status(result) == 200 && headers(result).get("Content-Type").get.contains("text/html"))
    })


  }


}
