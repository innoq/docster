package integration.formats

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import integration.helper.FakeApplicationHelper._
import integration.helper.WireMockHelper._
import integration.helper.{FakeApplicationHelper, WireMockHelper}
import org.jsoup.Jsoup
import org.scalatest.FlatSpec
import play.api.test.FakeRequest
import play.api.test.Helpers._

class HalIntegrationSpec extends FlatSpec {

  val json =
    """
      |{
      |  "_embedded" : {
      |    "restbucks:orders" : [ {
      |      "status" : "Zu zahlen",
      |      "orderedDate" : "2016-03-03T23:06:56.735",
      |      "_links" : {
      |        "self" : {
      |          "href" : "http://localhost:8080/orders/1"
      |        },
      |        "restbucks:order" : {
      |          "href" : "http://localhost:8080/orders/1{?projection}",
      |          "templated" : true,
      |          "title" : "Eine Bestellung"
      |        }
      |      }
      |    }, {
      |      "status" : "Zu zahlen",
      |      "orderedDate" : "2016-03-03T23:06:56.735",
      |      "_links" : {
      |        "self" : {
      |          "href" : "http://localhost:8080/orders/2"
      |        },
      |        "restbucks:order" : {
      |          "href" : "http://localhost:8080/orders/2{?projection}",
      |          "templated" : true,
      |          "title" : "Eine Bestellung"
      |        }
      |      }
      |    } ]
      |  },
      |  "_links" : {
      |    "self" : {
      |      "href" : "http://localhost:8080/orders"
      |    },
      |    "profile" : {
      |      "href" : "http://localhost:8080/profile/orders"
      |    },
      |    "search" : {
      |      "href" : "http://localhost:8080/orders/search"
      |    },
      |    "curies" : [ {
      |      "href" : "http://localhost:8080/docs/{rel}.html",
      |      "name" : "restbucks",
      |      "templated" : true
      |    } ]
      |  },
      |  "page" : {
      |    "size" : 20,
      |    "totalElements" : 2,
      |    "totalPages" : 1,
      |    "number" : 0
      |  }
      |}
    """.stripMargin


  behavior of "docster should return a HAL html representation that"

  it should "contains the name of the resource as title" in {

    val wireMockUri = randomUri
    val port = freePort()
    val server = new WireMockServer(port)
    val app = application(port)

    withAppAndMock(app, server, () => {
      server.stubFor(any(urlMatching(".*"))
        .withHeader("Accept", containing("application/hal+json"))
        .willReturn(aResponse()
          .withStatus(200)
          .withHeader("Content-Type", "application/hal+json")
          .withBody(json)))

      val result = route(app, FakeRequest("GET", randomUri)).get
      val responseStatus = status(result)
      val responseBody = contentAsString(result)
      val responseHeaders = headers(result)
      val document = Jsoup.parse(responseBody)
      assert(responseStatus == 200 && responseHeaders.get("Content-Type").exists(_.contains("text/html")) && document.title() == "Orders")
    })

  }


}
