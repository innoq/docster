package services


trait Transformer {

  def transform(request: ProxyRequest, response: ProxyResponse): Representation
}

