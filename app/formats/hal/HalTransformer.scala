package formats.hal

import play.api.libs.json.Json
import services._

object HalTransformer extends Transformer {

  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext

  override def transform(request: ProxyRequest, response: ProxyResponse): Documentation = {

    val body = Json.parse(response.body)
    val selfLink = (body \ "_links" \ "self" \ "href").asOpt[String]
    val title = selfLink.flatMap(_.split("/").lastOption).map(_.capitalize).getOrElse("undefined")

    Documentation(title = title)
  }
}
