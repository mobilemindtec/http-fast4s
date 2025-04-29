package io.http.fast4s.core

import io.http.fast4s.types.{Request, Response}
import io.micro.router.types.RouteEntry
import scala.collection.mutable

private[core] class HttpServerConfigs(
  var host: String = "0.0.0.0",
  var port: Int = 3000,
  var workers: Int = 1,
  var routes: mutable.ListBuffer[RouteEntry[Request, Response]] = mutable.ListBuffer(),
  var recover: Option[Recover] = None,
  var interceptors: mutable.Map[Int, Interceptor] = mutable.Map(),
  var leave: mutable.ListBuffer[NSLeave] = mutable.ListBuffer(),
  var enter: mutable.ListBuffer[NSEnter] = mutable.ListBuffer(),
  var async: Boolean = false
)
