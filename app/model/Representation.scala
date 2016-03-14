package model

import java.net.URI
import java.util.UUID

/**
 * Meta model of an hypermedia content type. We can use this to bridge between different formats.
 *
 * @param name name of the representation (e.g. "orders")
 * @param attributes all direct attributes not part of an embedded entity
 * @param relations relations to other resources
 * @param actions possible modifications
 * @param description human readable description
 * @param embeddedRepresentations representations are recursive data structures
 */
case class Representation(name: String, id: String = UUID.randomUUID().toString,  attributes: Option[JObject] = None, relations: Seq[Relation] = List.empty, actions: Seq[Action] = List.empty, description: Option[Description] = None, embeddedRepresentations: Map[String, List[Representation]] = Map.empty) extends Attribute

trait Attribute

case class JString(s: String) extends Attribute

case class JObject(obj: Map[String, Attribute]) extends Attribute

case class JArray(arr: List[Attribute]) extends Attribute

case class Description(description: Either[String, URI])

trait Action

case class Relation(key: String, uri: String, description: Option[Description] = None)
