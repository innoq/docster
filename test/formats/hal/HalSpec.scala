package formats.hal

import org.scalatest.FlatSpec
import services.{ProxyRequest, ProxyResponse}

class HalTransformerSpec extends FlatSpec {

  val anyRequest: ProxyRequest = ProxyRequest(uri = "something")

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

  behavior of "a HalTransformer"

  it should "use last path segment of the self ref as capitalized titel" in {
    val documentation = HalTransformer.transform(anyRequest, ProxyResponse(body = json))
    assert(documentation.title == "Orders")
  }

  it should "headline should contain the last capitalized path segment of the self ref" in {
    val documentation = HalTransformer.transform(anyRequest, ProxyResponse(body = json))
    assert(documentation.overview.headline.contains("Orders"))
  }

}
