package formats.hal

import java.io.StringReader

import com.theoryinpractise.halbuilder.api.{ContentRepresentation, RepresentationFactory}
import com.theoryinpractise.halbuilder.json.JsonRepresentationFactory
import services._

import scala.collection.JavaConversions._

object HalTransformer extends Transformer {

  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext

  val hal: RepresentationFactory = new JsonRepresentationFactory()

  override def transform(request: ProxyRequest, response: ProxyResponse): Documentation = {

    val representation = hal.readRepresentation(RepresentationFactory.HAL_JSON, new StringReader(response.body))

    val title = extractTitle(representation)
    val headline = title
    val relations = extractRelations(representation)

    Documentation(title, Overview(headline), relations)
  }

  private def extractTitle(representation: ContentRepresentation): String = {
    Option(representation.getResourceLink).flatMap(_.getHref.split("/").lastOption).map(_.capitalize).getOrElse("undefined")
  }

  private def extractRelations(representation: ContentRepresentation): List[Relation] = {
    representation.getLinks.toList.map { link =>
      Relation(link.getRel, link.getHref)
    }
  }

}
