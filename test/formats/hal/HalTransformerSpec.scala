package formats.hal

import java.net.URI

import model.{Relation, JArray, JObject, JString}
import org.scalatest.FlatSpec
import services._

class HalTransformerSpec extends FlatSpec {

  val anyRequest: ProxyRequest = ProxyRequest(uri = URI.create("http://example.com"))

  val springRestJson =
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

  it should "use last path segment of the self ref as name" in {
    val representation = HalTransformer.transform(anyRequest, ProxyResponse(body = springRestJson))
    assert(representation.name == "Orders")
  }

  it should " add all links to a relations section" in {
    val documentation = HalTransformer.transform(anyRequest, ProxyResponse(body = springRestJson))
    val navigations = List(
      Relation("self", "http://localhost:8080/orders"),
      Relation("profile", "http://localhost:8080/profile/orders"),
      Relation("search", "http://localhost:8080/orders/search")
    )

    assert(documentation.relations == navigations)
  }


  val orderServiceJson =
    """
      |{
      |  "id" : 442820205,
      |  "date" : "2014-10-03",
      |  "updated" : "2014-10-03",
      |  "billingAddress" : "Bruxelles, Belgium",
      |  "shippingAddress" : "Bruxelles, Belgium",
      |  "status" : "cancelled",
      |  "total" : 2090,
      |  "_links" : {
      |    "om:cancellation" : [ {
      |      "href" : "http://localhost:7777/cancellations/0",
      |      "templated" : false
      |    } ],
      |    "curies" : [ {
      |      "href" : "http://example.com/rels/ordermanager/{rel}",
      |      "name" : "om",
      |      "templated" : true
      |    } ],
      |    "self" : [ {
      |      "href" : "http://localhost:7777/orders/442820205",
      |      "templated" : false
      |    } ]
      |  },
      |  "_embedded" : {
      |    "om:orderItems" : [ {
      |      "_links" : {
      |        "self" : [ {
      |          "href" : "http://localhost:7777/orders/442820205/items",
      |          "templated" : false
      |        } ]
      |      },
      |      "_embedded" : {
      |        "item" : [ {
      |          "quantity" : 1,
      |          "_links" : {
      |            "self" : [ {
      |              "href" : "http://localhost:7777/orders/442820205/items/0",
      |              "templated" : false
      |            } ]
      |          },
      |          "_embedded" : {
      |            "om:product" : [ {
      |              "description" : "Laptop X65",
      |              "price" : 799,
      |              "_links" : {
      |                "self" : [ {
      |                  "href" : "http://prod.example.com/products/352",
      |                  "templated" : false
      |                } ]
      |              }
      |            } ]
      |          }
      |        }, {
      |          "quantity" : 4,
      |          "_links" : {
      |            "self" : [ {
      |              "href" : "http://localhost:7777/orders/442820205/items/1",
      |              "templated" : false
      |            } ]
      |          },
      |          "_embedded" : {
      |            "om:product" : [ {
      |              "description" : "1250GB HD",
      |              "price" : 199,
      |              "_links" : {
      |                "self" : [ {
      |                  "href" : "http://prod.example.com/products/123",
      |                  "templated" : false
      |                } ]
      |              }
      |            } ]
      |          }
      |        }, {
      |          "quantity" : 5,
      |          "_links" : {
      |            "self" : [ {
      |              "href" : "http://localhost:7777/orders/442820205/items/2",
      |              "templated" : false
      |            } ]
      |          },
      |          "_embedded" : {
      |            "om:product" : [ {
      |              "description" : "MP3 Player",
      |              "price" : 99,
      |              "_links" : {
      |                "self" : [ {
      |                  "href" : "http://prod.example.com/products/231",
      |                  "templated" : false
      |                } ]
      |              }
      |            } ]
      |          }
      |        } ]
      |      }
      |    } ],
      |    "om:customer" : [ {
      |      "description" : "Tim",
      |      "_links" : {
      |        "self" : [ {
      |          "href" : "http://crm.example.com/customers/4123",
      |          "templated" : false
      |        } ]
      |      }
      |    } ]
      |  }
      |}
    """.stripMargin

  ignore should "transform embedded entities" in {

    val representation = HalTransformer.transform(anyRequest, ProxyResponse(body = orderServiceJson))

  }


  it should "transform simple attributes" in {

    val json =
      """
        | {
        | "id": 442820205,
        | "price": 12.3,
        | "done": true,
        | "age": 1
        |}
      """.stripMargin

    val representation = HalTransformer.transform(anyRequest, ProxyResponse(body = json))

    val expectedAttributes = JObject(Map(
      ("id", JString("442820205")),
      ("price", JString("12.3")),
      ("done", JString("true")),
      ("age", JString("1"))))

    assert(representation.attributes.get == expectedAttributes)
  }


  it should "transform array with simple attributes" in {

    val json =
      """
        |{
        |    "status": "fine",
        |    "ids": [
        |        12,
        |        14,
        |        15,
        |        18
        |    ]
        |}
      """.stripMargin

    val representation = HalTransformer.transform(anyRequest, ProxyResponse(body = json))

    val expectedAttributes = JObject(Map(
      ("status", JString("fine")),
      ("ids", JArray(List(
        JString("12"),
        JString("14"),
        JString("15"),
        JString("18"))))))

    assert(representation.attributes.get == expectedAttributes)
  }


  it should "transform object properties" in {

    val json =
      """
        |{
        |    "object": {
        |        "name": "myObject",
        |        "status": "cancelled"
        |    }
        |}
      """.stripMargin

    val representation = HalTransformer.transform(anyRequest, ProxyResponse(body = json))

    val expectedAttributes = JObject(Map(
    ("object", JObject(Map(
      ("name", JString("myObject")),
      ("status", JString("cancelled")))))))

    assert(representation.attributes.get == expectedAttributes)
  }

  it should "transform arrays that contains objects" in {

    val json =
      """
        |{
        |    "objects": [
        |        {
        |            "name": "myObject1",
        |            "status": "cancelled"
        |        },
        |        {
        |            "name": "myObject2",
        |            "status": "cancelled"
        |        }
        |    ]
        |}
      """.stripMargin

    val representation = HalTransformer.transform(anyRequest, ProxyResponse(body = json))

    val expectedAttributes = JObject(Map(
    ("objects", JArray(List(
     JObject(Map(
      ("name", JString("myObject1")),
      ("status", JString("cancelled")))),
     JObject(Map(
      ("name", JString("myObject2")),
      ("status", JString("cancelled")))))
      ))))

    assert(representation.attributes.get == expectedAttributes)
  }

}
