package integration.proxy

import integration.helper.FakeApplicationHelper.{application, withApplication}
import org.jsoup.Jsoup
import org.scalatest.FlatSpec
import play.api.test.FakeRequest
import play.api.test.Helpers._

class SmokeIntegrationSpec extends FlatSpec {

  behavior of "docster"

  it should "be able to transform a public hal response" in {

    val app = application("www.mocky.io")

    withApplication(app) { () =>
      val request = FakeRequest("GET", "/v2/56ddea041100006218d086ab").withHeaders(("Accept", "text/html"), ("Host", "localhost:9000"))
      val response = route(app, request).get

      val resStatus = status(response)
      val resContentType = header("Content-Type", response).get
      val resContent = Jsoup.parse(contentAsString(response))
      val resTitle = resContent.title()

      assert(resStatus == 200 && resContentType.contains("text/html") && resTitle == "Orders", s"response=$resContent")
    }
  }

}
