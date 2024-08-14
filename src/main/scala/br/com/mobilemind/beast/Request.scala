package br.com.mobilemind.beast

import br.com.mobilemind.micro.routing.{Params, Query, RouteMatcher}

case class Request(override val target: String,
              override val method: HttpMethod,
              override val contentType: Option[ContentType],
              override val body: Option[String] = None,
              override val bodyRaw: Option[Seq[Byte]] = None,
              override val headers: Headers = Map(),
              override val params: Params = Params(),
              override val query: Query = Query(),
              override val matcher: RouteMatcher = Nil)
  extends HttpRequest

case class RequestEntity[T](override val target: String,
              override val method: HttpMethod,
              override val contentType: Option[ContentType],
              override val body: Option[String] = None,
              override val bodyRaw: Option[Seq[Byte]] = None,
              override val headers: Headers = Map(),
              override val params: Params = Params(),
              override val query: Query = Query(),
              override val matcher: RouteMatcher = Nil,
               entity: Option[T] = None)
  extends HttpRequest

object RequestEntity:
  extension[T] (req: Request)
    def toRequestEntity(entity: Option[T]): HttpRequest =
      RequestEntity(
        req.target,
        req.method,
        req.contentType,
        req.body,
        req.bodyRaw,
        req.headers,
        req.params,
        req.query,
        req.matcher,
        entity
      )
