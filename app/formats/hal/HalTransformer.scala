package formats.hal

import java.io.StringReader
import java.util

import com.theoryinpractise.halbuilder.api.{ReadableRepresentation, RepresentationFactory}
import com.theoryinpractise.halbuilder.json.JsonRepresentationFactory
import services._

import scala.collection.JavaConversions._

/**
 * Transforms <code>application/hal+json</code into <code>text/html</code>
 */
object HalTransformer extends ContentTypeTransformer {

  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext

  val hal: RepresentationFactory = new JsonRepresentationFactory()

  private def extractAttributes(representation: ReadableRepresentation): Option[JObject] = {

    representation.getProperties.toMap match {
      case properties if properties.isEmpty => None
      case notEmpty => Some(JObject(notEmpty.mapValues(toAttribute)))
    }
  }

  private def toAttribute(thing: AnyRef): Attribute = {
    thing match {
      case x: String => JString(x)
      case x: java.lang.Integer => JString(x + "")
      case x: java.lang.Double => JString(x + "")
      case x: java.lang.Boolean => JString(x + "")
      case x: util.ArrayList[AnyRef] => JArray(x.toList.map(toAttribute))
      case x: util.Map[String, AnyRef] => JObject(x.toMap.mapValues(toAttribute))
      case default => throw new IllegalStateException(s"unexpected type: $default.type")
    }
  }

  private def extractTitle(representation: ReadableRepresentation): String = {
    Option(representation.getResourceLink).flatMap(_.getHref.split("/").lastOption).map(_.capitalize).getOrElse("undefined")
  }

  private def extractRelations(representation: ReadableRepresentation): List[Relation] = {
    representation.getLinks.toList.map { link =>
      Relation(link.getRel, link.getHref)
    }
  }

  override def transform(request: ProxyRequest, response: ProxyResponse): Representation = {
    val representation = hal.readRepresentation(RepresentationFactory.HAL_JSON, new StringReader(response.body))
    toRepresentation(representation)
  }

  def toRepresentation(representation: ReadableRepresentation): Representation = {

    val name = extractTitle(representation)
    val links = extractRelations(representation)
    val atts = extractAttributes(representation)
    val embeddeds = representation.getResourceMap.toMap.map {
      case (key, halRepresentation: util.Collection[ReadableRepresentation]) => (key, halRepresentation.toList.map(toRepresentation))
    }

    Representation(name, navigations = links, attributes = atts, embeddedRepresentations = embeddeds)
  }

  override def from: String = "application/hal+json"
}
