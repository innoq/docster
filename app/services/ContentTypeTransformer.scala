package services

import model.Representation

/**
 * Could transform one Content-Type into another based on the original request and response.
 */
trait ContentTypeTransformer {

  /**
   * Target content type.
   */
  def from: String

  def transform(request: ProxyRequest, response: ProxyResponse): Representation
}

