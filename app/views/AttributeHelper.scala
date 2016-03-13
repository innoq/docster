package views

import model.{Attribute, JArray, JObject, JString}

/**
 * Helper to render attributes.
 *
 * Not part of the template as it's a bit too complex and needs testing.
 * Another approach would be to have recursive templates (see representationSection), maybe that would be better...
 */
object AttributeHelper {

  def renderAttribute(rootAttribute: JObject): String = {

    //TODO make tail recursive by explicitly modeling object start end end tags (at least end tags)
    //@tailrec
    def _renderAttribute(attribute: Attribute, acc: StringBuilder): StringBuilder = {
      attribute match {
        case JObject(fields) =>
          acc ++= "<dl class=\"dl-horizontal\">\n"
          fields.foldLeft(acc)((acc, field) => _renderAttribute(field._2, acc ++= s"<dt>${field._1}</dt>\n"))
          acc ++= "</dl>\n"
        case JArray(attributes) =>
          attributes.foldLeft(acc)((acc, attribute) => _renderAttribute(attribute, acc))
        case JString(s) => acc ++= s"<dd>$s</dd>\n"
      }
    }

    _renderAttribute(rootAttribute, new StringBuilder()).toString()
  }

}



