package io.http.fast4s.bindings

import io.http.fast4s.bindings.structs.*
import io.http.fast4s.types.{ContentType, HttpMethod, HttpStatus, Request, Response}

import scala.language.experimental.namedTuples
import scala.scalanative.*
import scala.scalanative.unsafe.{CString, Ptr, Zone, fromCString}

type Headers = Map[String, String]

object conv:

  extension (cs: CString)
    def str = fromCString(cs)

  extension (s: String)(using Zone)
    def c_str =
      unsafe.toCString(s)

  extension(reqPtr: Ptr[BeastRequest])
    def request(): Request =
      val req = !reqPtr
      Request(
        method = HttpMethod(req.method),
        target = req.target,
        body = req.body,
        contentType = req.contentType.map(ContentType.make),
        rawBody = req.rawBody,
        headers = req.headers,
      )

  extension(respPtr: Ptr[BeastResponse])
    def response(): Response =
      val resp = !respPtr
      Response(
        status = HttpStatus.make(resp.status),
        contentType = ContentType.make(resp.contentType.getOrElse("")),
        body = resp.body,
        rawBody = resp.rawBody,
        headers = resp.headers,
      )

  extension (req: Request)(using Zone)
    def ptr(): Ptr[BeastRequest] =
      BeastRequest(
        req.method.verb.c_str,
        req.target.c_str,
        req.contentType.map(_.mimeType.c_str).orNull,
        BeastBody(req.body, req.rawBody),
        BeastHeaders(req.headers))


  extension(resp: Response)
    def ptr()(using Zone): Ptr[BeastResponse] =
      BeastResponse(
        status = resp.status.code,
        contentType = resp.contentType.mimeType,
        body = resp.body,
        rawBody = resp.rawBody,
        headers = resp.headers)

