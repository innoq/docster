package services

import java.net.URI

sealed abstract class Attribute

case class JString(s: String) extends Attribute

case class JObject(obj: Map[String, Attribute]) extends Attribute

case class JArray(arr: List[Attribute]) extends Attribute

case class Description(description: Either[String, URI])

case class Representation(name: String, attributes: Option[JObject] = None, navigations: Seq[Relation] = List.empty, actions: Seq[Action] = List.empty, description: Option[Description] = None) extends Attribute

trait Action

case class Relation(key: String, uri: String, description: Option[Description] = None)
