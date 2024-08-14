package br.com.mobilemind.beast

import br.com.mobilemind.micro.routing.{Params, Query, RouteMatcher}

trait HttpRequest:
  val target: String
  val method: HttpMethod
  val contentType: Option[ContentType]
  val body: Option[String] = None
  val bodyRaw: Option[Seq[Byte]] = None
  val headers: Headers = Map()
  val query: Query
  val params: Params
  val matcher: RouteMatcher

case class ReqExtra(body: Option[String] = None,
                    bodyRaw: Option[Seq[Byte]] = None,
                    headers: Headers = Map())