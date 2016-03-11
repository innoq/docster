package services

import java.net.URI

sealed abstract class Attribute

case class JString(s: String) extends Attribute

case class JObject(obj: Map[String, Attribute]) extends Attribute

case class JArray(arr: List[Attribute]) extends Attribute

case class Description(description: Either[String, URI])

case class Representation(name: String, attributes: Option[JObject] = None, navigations: Seq[Relation] = List.empty, actions: Seq[Action] = List.empty, description: Option[Description] = None, embeddedRepresentations: Map[String, List[Representation]] = Map.empty) extends Attribute

trait Action

trait Relation {
  def description: Option[Description]

  def target: String

  def key: String
}

case class Link(key: String, uri: URI, description: Option[Description] = None) extends Relation {

  def target = uri.toString
}

case class Form(key: String, uri: String, description: Option[Description] = None) extends Relation {

  def target = uri
}
