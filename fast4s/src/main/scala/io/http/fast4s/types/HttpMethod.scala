package io.http.fast4s.types

import io.micro.router.types.Method

enum HttpMethod(val verb: String):
  case Head extends HttpMethod("HEAD")
  case Options extends HttpMethod("OPTIONS")
  case Patch extends HttpMethod("PATCH")
  case Get extends HttpMethod("GET")
  case Post extends HttpMethod("POST")
  case Put extends HttpMethod("PUT")
  case Delete extends HttpMethod("DELETE")
  case Trace extends HttpMethod("TRACE")
  case Connect extends HttpMethod("CONNECT")
  case Other(s: String) extends HttpMethod(s)

object HttpMethod:
  def apply(verb: String): HttpMethod =
    val values = Head :: Options :: Patch :: Get :: Post :: Put :: Delete :: Connect :: Trace :: Nil
    values.find(m => m.verb.equals(verb)).getOrElse(Other(verb))

extension (method: HttpMethod)
  def toMethod: Method = method match
    case HttpMethod.Get => Method.GET
    case HttpMethod.Post => Method.POST
    case HttpMethod.Put => Method.PUT
    case HttpMethod.Delete => Method.DELETE
    case HttpMethod.Options => Method.OPTIONS
    case HttpMethod.Patch => Method.PATCH
    case HttpMethod.Head => Method.HEAD
    case HttpMethod.Connect => Method.CONNECT
    case HttpMethod.Trace => Method.TRACE
    case _ => Method.ANY

extension (method: Method)
  def toHttpMethod: HttpMethod = method match
    case Method.GET => HttpMethod.Get
    case Method.POST => HttpMethod.Post
    case Method.PUT => HttpMethod.Put
    case Method.DELETE => HttpMethod.Delete
    case Method.OPTIONS => HttpMethod.Options
    case Method.PATCH => HttpMethod.Patch
    case Method.HEAD => HttpMethod.Head
    case Method.CONNECT => HttpMethod.Connect
    case Method.TRACE => HttpMethod.Trace
    case Method.ANY => HttpMethod.Other("")