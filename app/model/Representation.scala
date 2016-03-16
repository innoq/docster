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
case class Representation(name: String, attributes: Option[JObject] = None, relations: Seq[Relation] = List.empty, actions: Seq[Action] = List.empty, description: Option[Description] = None, embeddedRepresentations: Map[String, List[Representation]] = Map.empty) extends Attribute with Identifiable

trait Identifiable {

  def id = UUID.randomUUID()

}

trait Attribute

case class JString(s: String) extends Attribute

case class JObject(obj: Map[String, Attribute]) extends Attribute

case class JArray(arr: List[Attribute]) extends Attribute

case class Description(description: Either[String, URI])

case class Action(relation: Relation, contentType: String, fields: List[Field], method: List[HttpMethod])

case class Field(name: String, fieldType: FieldType, defaultText: Option[String])

case class Relation(key: String, uri: String, description: Option[Description] = None)


trait HttpMethod

case object Post extends HttpMethod

case object Put extends HttpMethod

case object Get extends HttpMethod

case object Delete extends HttpMethod


trait FieldType

case object Text extends FieldType

case object Search extends FieldType

case object Tel extends FieldType

case object Url extends FieldType

case object Email extends FieldType

case object Password extends FieldType

case object Datetime extends FieldType

case object Date extends FieldType

case object Month extends FieldType

case object Week extends FieldType

case object Time extends FieldType

case object Number extends FieldType

case object Range extends FieldType

case object Color extends FieldType

case object Checkbox extends FieldType

case object Radio extends FieldType

case object File extends FieldType
