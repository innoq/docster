package services


case class ProxyRequest(method: String, uri: String, headers: Map[String, Seq[String]], body: String) {

  def simpleHeaderMap: Map[String, String] = {
    headers.map {
      case (k, v) => (k, v.mkString(","))
    }
  }

  def putHeader(key: String, value: Seq[String]): ProxyRequest = {
    copy(headers = headers.updated(key, value))
  }

}

