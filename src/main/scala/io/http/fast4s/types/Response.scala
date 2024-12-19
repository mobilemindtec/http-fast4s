package io.http.fast4s.types

import io.http.fast4s.bindings.Headers

import scala.collection.immutable

case class Response(
    status: HttpStatus,
    body: Option[String] = None,
    contentType: ContentType = ContentType.Text,
    rawBody: Option[immutable.Seq[Byte]] = None,
    headers: Headers = Map()
)
object Response:

  def ok(body: String = "", contentType: ContentType = ContentType.Text): Response =
    Response(HttpStatus.OK, contentType = contentType, body = Some(body))

  def notFound(body: String = "Not Found", contentType: ContentType = ContentType.Text): Response =
    Response(HttpStatus.NotFound, contentType = contentType, body = Some(body))

  def serverError(body: String = "Internal Server Error", contentType: ContentType = ContentType.Text): Response =
    Response(HttpStatus.InternalServerError, contentType = contentType, body = Some(body))

  def badRequest(body: String = "Bad Request", contentType: ContentType = ContentType.Text): Response =
    Response(HttpStatus.BadRequest, contentType = contentType, body = Some(body))
