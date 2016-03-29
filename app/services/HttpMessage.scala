package services

case class HttpMessage(headers: Map[String, Seq[String]] = Map.empty, body: Option[String] = None)
