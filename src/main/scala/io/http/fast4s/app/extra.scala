package io.http.fast4s.app

import io.http.fast4s.types.*
import io.micro.router.{RequestBuilder, RouteInfo}
import language.experimental.modularity

object extra:

  type ReqBuilder = RequestBuilder[Request, RawRequest]

  given ReqBuilder:
    def build(routeInfo: RouteInfo,
              extra: Option[RawRequest]): Request =

      val headers = extra.map(_.headers).getOrElse(Map())
      val contentType = headers
        .find(_._1.toLowerCase == "content-type")
        .map(_._2)
        .map(ContentType.make)
        .getOrElse(ContentType.Empty)

      Request(
        routeInfo.method.toHttpMethod,
        routeInfo.target,
        extra.map(_.body).getOrElse(""),
        contentType,
        extra.map(_.bodyRaw).getOrElse(Nil),
        headers,
        routeInfo.params,
        routeInfo.query,
        routeInfo.matcher
      )
