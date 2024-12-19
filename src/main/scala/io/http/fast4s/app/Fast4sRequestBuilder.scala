package io.http.fast4s.app

import io.http.fast4s.types.*
import io.micro.router.{RequestBuilder, RouteInfo}
import language.experimental.modularity

object Fast4sRequestBuilder:

  given RequestBuilder[Request, RawRequest]:
    def build(routeInfo: RouteInfo,
                       extra: Option[RawRequest]
                      ): Request =

      val headers = extra.map(_.headers).getOrElse(Map())
      val contentType = headers
        .find(_._1.toLowerCase == "content-type")
        .map(_._2)
        .map(ContentType.make)

      Request(
        routeInfo.method.toHttpMethod,
        routeInfo.target,
        extra.flatMap(_.body),
        contentType,
        extra.flatMap(_.bodyRaw),
        headers,
        routeInfo.params,
        routeInfo.query,
        routeInfo.matcher
      )
