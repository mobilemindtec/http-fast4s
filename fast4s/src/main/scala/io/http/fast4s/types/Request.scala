package io.http.fast4s.types

import io.http.fast4s.beast.Headers
import io.micro.router.types.{Params, Query, RouteMatcher}

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