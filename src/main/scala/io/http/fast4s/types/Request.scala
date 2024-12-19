package io.http.fast4s.types

import io.http.fast4s.bindings.Headers
import io.micro.router.core.{Params, Query, RouteMatcher}
import scala.collection.immutable

case class Request(
    method: HttpMethod,
    target: String,
    body: Option[String] = None,
    contentType: Option[ContentType] = None,
    rawBody: Option[immutable.Seq[Byte]] = None,
    headers: Headers = Map(),
    params: Params = Params(),
    query: Query = Query(),
    matcher: RouteMatcher = Nil
)