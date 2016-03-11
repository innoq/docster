package services


trait ContentTypeTransformer {

  def from: String

  def transform(request: ProxyRequest, response: ProxyResponse): Representation
}

