package io.http.fast4s.types

enum ContentType(val mimeType: String):
  case Json extends ContentType("application/json")
  case JavaScript extends ContentType("application/javascript")
  case Html extends ContentType("text/html")
  case Text extends ContentType("text/plain")
  case CSS extends ContentType("text/css")
  case PNG extends ContentType("image/png")
  case JPG extends ContentType("image/jpg")
  case JPEG extends ContentType("image/jpeg")
  case Empty extends ContentType("")
  case Other(s: String) extends ContentType(s)

object ContentType:
  def fromMimeType(mimeType: String): ContentType =
    Other(MimeType.get(mimeType).getOrElse("text/plain"))

  def make(text: String): ContentType =
    val options = List(
      Json, JavaScript, Html, Text, CSS, PNG, JPG, JPEG
    )
    options
      .find(_.mimeType == text)
      .getOrElse(Other(text))

