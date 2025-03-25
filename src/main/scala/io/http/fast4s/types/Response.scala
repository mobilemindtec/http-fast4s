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

  object Ok:
    def apply(body: String = HttpStatus.OK.reason, contentType: ContentType = ContentType.Text): Response =
      ok(body, contentType)

  object NotFound:
    def apply(body: String = HttpStatus.NotFound.reason, contentType: ContentType = ContentType.Text): Response =
      notFound(body, contentType)

  object ServerError:
    def apply(body: String = HttpStatus.InternalServerError.reason, contentType: ContentType = ContentType.Text): Response =
      serverError(body, contentType)

  object BadRequest:
    def apply(body: String = HttpStatus.BadRequest.reason, contentType: ContentType = ContentType.Text): Response =
      badRequest(body, contentType)

  object Created:
    def apply(body: String = HttpStatus.Created.reason, contentType: ContentType = ContentType.Text): Response =
      created(body, contentType)

  object Unauthorized:
    def apply(body: String = HttpStatus.Unauthorized.reason, contentType: ContentType = ContentType.Text): Response =
      unauthorized(body, contentType)

  object Forbidden:
    def apply(body: String = HttpStatus.Forbidden.reason, contentType: ContentType = ContentType.Text): Response =
      forbiden(body, contentType)

  def ok(body: String = "", contentType: ContentType = ContentType.Text): Response =
    Response(HttpStatus.OK, contentType = contentType, body = Some(body))

  def created(body: String, contentType: ContentType = ContentType.Text): Response =
    Response(HttpStatus.Created, contentType = contentType, body = Some(body))

  def notFound(body: String = HttpStatus.NotFound.reason, contentType: ContentType = ContentType.Text): Response =
    Response(HttpStatus.NotFound, contentType = contentType, body = Some(body))

  def serverError(body: String = HttpStatus.InternalServerError.reason, contentType: ContentType = ContentType.Text): Response =
    Response(HttpStatus.InternalServerError, contentType = contentType, body = Some(body))

  def badRequest(body: String = HttpStatus.BadRequest.reason, contentType: ContentType = ContentType.Text): Response =
    Response(HttpStatus.BadRequest, contentType = contentType, body = Some(body))

  def unauthorized(body: String = HttpStatus.Unauthorized.reason, contentType: ContentType = ContentType.Text): Response =
    Response(HttpStatus.Unauthorized, contentType = contentType, body = Some(body))

  def forbiden(body: String = HttpStatus.Forbidden.reason, contentType: ContentType = ContentType.Text): Response =
    Response(HttpStatus.Forbidden, contentType = contentType, body = Some(body))
