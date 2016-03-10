package services

import java.net.URI

trait Transformer {

  def transform(request: ProxyRequest, response: ProxyResponse): Representation
}


trait Attribute

case class Description(description: Either[String, URI])

case class Representation(name: String, attributes: Map[String, Attribute] = Map(), navigations: Seq[Navigation], actions: Seq[Action] = List.empty, description: Option[Description] = None) extends Attribute

case class CollectionAttribute(attributes: Seq[Attribute]) extends Attribute

case class Property(value: String) extends Attribute

trait Action

case class Navigation(key: String, uri: String, description: Option[Description] = None)

