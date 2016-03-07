package services

import play.api.http.MediaRange

object MediaRanges {

  def toMediaRanges(mimeTypes: Iterable[String]): Seq[MediaRange] = {
    toMediaRanges(mimeTypes.toList: _*)
  }


  def toMediaRanges(mimeTypes: String*): Seq[MediaRange] = {
    MediaRange.parse(mimeTypes.mkString(","))
  }

  def toMediaRange(mimeType: String): MediaRange = {
    MediaRange.parse(mimeType).head
  }

}
