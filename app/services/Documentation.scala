package services


case class Documentation(title: String, overview: Overview, relations: Seq[Relation])

case class Overview(headline: String)

case class Relation(key: String, uri: String, description: Option[String]  = None)
