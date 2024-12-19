package io.http.fast4s

import io.http.fast4s.app.Fast4s.app
import io.http.fast4s.types.{HttpMethod, HttpStatus, Request, Response}
import org.scalatest.funsuite.AnyFunSuite

class ServerHandleSpec extends AnyFunSuite:

  extension (s: String) def some: Option[String] = Some(s)

  test("request test"){

    app.get("/user") {
      req =>
        req.body match
          case None => Response.ok("pong")
          case _ => Response.notFound()
    }

    app.post("/user") {
      req =>
        req.body match
          case Some("ping") => Response.ok("pong")
          case _ => Response.badRequest()
    }

    app.get("/user/:id") {
      (req: Request) =>
        req.body match
          case Some("ping") => Response.ok("pong")
          case _ => Response.badRequest()
    }

    val server = app.server.create()

    server
      .handle(Request(HttpMethod.Get, "/user")) match
        case Response(HttpStatus.OK, Some("pong"), _, _, _) =>
        case resp =>  fail(s"expected pong: $resp")

    server
      .handle(Request(HttpMethod.Get, "/users")) match
      case Response(HttpStatus.NotFound, _, _, _, _) =>
      case resp =>  fail(s"expected not found: $resp")

    server
      .handle(Request(HttpMethod.Get, "/user", "ping".some)) match
      case Response(HttpStatus.OK, Some("pong"), _, _, _) =>
      case resp =>  fail(s"expected pong: $resp")
  }




