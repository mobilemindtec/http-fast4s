package io.app

import io.http.fast4s.api.{*, given}
import io.micro.router.api.*

object AppServer:

  type RouteEnter = Enter[Request, Response]
  type Route = RouteEntry[Request, Response]

  def serve =

    val logger: RouteEnter = enter(GET):
      req =>
        println(s"enter in ${req.target}")
        req

    val home: Route = route(GET, root):
      req => Response.ok("alive!")

    val ping: Route = route(GET, root / "ping"):
      case _ => Response.ok("pong")

    newHttpServerBuilder
      .withRoutes(
        logger ++ home,
        logger ++ ping
      )
      .build
      .serve



