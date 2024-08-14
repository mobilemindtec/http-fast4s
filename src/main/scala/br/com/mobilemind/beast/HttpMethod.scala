package br.com.mobilemind.beast

import br.com.mobilemind.micro.routing.router.Method

enum HttpMethod(val verb: String):
  case Head extends HttpMethod("HEAD")
  case Options extends HttpMethod("OPTIONS")
  case Patch extends HttpMethod("PATCH")
  case Get extends HttpMethod("GET")
  case Post extends HttpMethod("POST")
  case Put extends HttpMethod("PUT")
  case Delete extends HttpMethod("DELETE")
  case Other(s: String) extends HttpMethod(s)

object HttpMethod:
  def apply(verb: String): HttpMethod =
    val values = Head :: Options :: Patch :: Get :: Post :: Put :: Delete :: Nil
    values.find(m => m.verb.equals(verb)).getOrElse(Other(verb))

extension (method: HttpMethod)
  def toMethod: Method = method match
    case HttpMethod.Get => Method.Get
    case HttpMethod.Post => Method.Post
    case HttpMethod.Put => Method.Put
    case HttpMethod.Delete => Method.Delete
    case HttpMethod.Options => Method.Options
    case HttpMethod.Patch => Method.Patch
    case HttpMethod.Head => Method.Head
    case _ => Method.Any

extension (method: Method)
  def toHttpMethod: HttpMethod = method match
    case Method.Get => HttpMethod.Get
    case Method.Post => HttpMethod.Post
    case Method.Put => HttpMethod.Put
    case Method.Delete => HttpMethod.Delete
    case Method.Options => HttpMethod.Options
    case Method.Patch => HttpMethod.Patch
    case Method.Head => HttpMethod.Head
    case Method.Any => HttpMethod.Other("")