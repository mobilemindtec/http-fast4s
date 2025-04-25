package io.http.fast4s.types

import io.http.fast4s.bindings.Headers
import io.micro.router.core.{Params, Query, RouteMatcher}
import scala.collection.immutable

case class Request(
    method: HttpMethod,
    target: String,
    body: String = "",
    contentType: ContentType = ContentType.Empty,
    rawBody: Seq[Byte] = Nil,
    headers: Headers = Map(),
    params: Params = Params(),
    query: Query = Query(),
    matcher: RouteMatcher = Nil
)