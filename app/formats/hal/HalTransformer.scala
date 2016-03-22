package formats.hal

import java.io.StringReader
import java.net.URI
import java.util

import com.theoryinpractise.halbuilder.api.{Link, ReadableRepresentation, RepresentationFactory}
import com.theoryinpractise.halbuilder.json.JsonRepresentationFactory
import model._
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
      case x: util.ArrayList[AnyRef @unchecked] => JArray(x.toList.map(toAttribute))
      case x: util.Map[String @unchecked, AnyRef @unchecked] => JObject(x.toMap.mapValues(toAttribute))
      case default => throw new IllegalStateException(s"unexpected type: $default.type")
    }
  }

  private def extractTitle(representation: ReadableRepresentation): String = {

    def isNumber(string: String): Boolean = {
      string.forall(_.isDigit)
    }

    val uriPattern = "https?:\\/\\/(.*?:?\\d*)(\\/.*)".r
    val selfRef: Option[Link] = Option(representation.getResourceLink)
    val path = selfRef.flatMap { link =>
      uriPattern.findFirstMatchIn(link.getHref).map(_.group(2))
    }
    path match {
      case Some("/") => "Home"
      case Some(s) =>
        val pathParts = s.split("/").filter(_ != "")
        val lastPathSegmentAsNumber: Option[String] = pathParts.reverse.headOption.find(isNumber)
        val firstNotNumberPart: Option[String] = pathParts.reverse.find(!isNumber(_))
        val resultName = (lastPathSegmentAsNumber, firstNotNumberPart) match {
          case (None, None) => "Undefined"
          case (None, Some(word)) => word // single resource
          case (Some(number), None) => number // what ever
          case (Some(number), Some(word)) => s"${word.stripSuffix("s")} #$number" // collection resource
        }
        resultName.trim.capitalize

      case None => "Undefined"
    }
  }

  private def extractRelations(representation: ReadableRepresentation): List[Relation] = {

    case class Rel(namespace: Option[String], key: String)


    def toRel(string: String): Rel = {
      val groups = string.split(":").toList
      groups match {
        case List() => throw new IllegalStateException(s"relation without rel key: $string")
        case List(a) => Rel(None, a)
        case List(a, b) => Rel(Some(a), b)
        case default => throw new IllegalStateException(s"relation without unparseable rel key: $string")
      }
    }

    def toDescription(rel: Rel): Option[Description] = {
      val namespaces: Map[String, String] = representation.getNamespaces.toMap
      val linkDocUriTemplate: Option[String] = rel.namespace.flatMap(namespaces.get)
      val linkDocUri: Option[String] = linkDocUriTemplate.map(_.replace("{rel}", rel.key))
      val descriptionLink: Option[URI] = linkDocUri.map(URI.create)
      descriptionLink.map { uri: URI =>
        Description(Right(uri))
      }
    }

    representation.getLinks.toList.map { link =>
      val linkRel = toRel(link.getRel)
      Relation(linkRel.key, link.getHref, toDescription(linkRel))
    }
  }

  private def extractActions(relations: List[Relation]): List[Action] = {
    relations.filter(_.key != "self").map { rel =>
      Action(rel, "applicaton/hal+json", List(), List(Post, Put, Delete))
    }
  }

  override def transform(request: ProxyRequest, response: ProxyResponse): Representation = {
    val representation = hal.readRepresentation(RepresentationFactory.HAL_JSON, new StringReader(response.body))
    toRepresentation(representation)
  }

  def toRepresentation(representation: ReadableRepresentation): Representation = {

    val name = extractTitle(representation)
    val rels = extractRelations(representation)
    val atts = extractAttributes(representation)
    val embeddeds = representation.getResourceMap.toMap.map {
      case (key, halRepresentation: util.Collection[ReadableRepresentation]) => (key, halRepresentation.toList.map(toRepresentation))
    }
    val actions = extractActions(rels)

    Representation(name, relations = rels, attributes = atts, embeddedRepresentations = embeddeds, actions = actions)
  }

  override def from: String = "application/hal+json"
}
