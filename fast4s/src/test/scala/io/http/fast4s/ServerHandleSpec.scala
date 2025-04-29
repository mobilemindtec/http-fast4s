package fast4s

import fast4s.app.Fast4s.app
import fast4s.types.{HttpMethod, HttpStatus, Request, Response}
import fast4s.types.Response.*
import org.scalatest.funsuite.AnyFunSuite

class ServerHandleSpec extends AnyFunSuite:

  test("request test"){

    app.get("/user") {
      _ => Ok("pong")
    }

    app.post("/user") {
      req =>
        req.body match
          case "ping" => Ok("pong")
          case _ => BadRequest()
    }

    app.get("/user/:id") {
      (req: Request) =>
        req.body match
          case "ping" => Ok("pong")
          case _ => BadRequest()
    }

    val server = app.server.create()

    server
      .handle(Request(HttpMethod.Get, "/user")) match
        case Response(HttpStatus.OK, "pong", _, _, _) =>
        case resp =>  fail(s"expected pong: $resp")

    server
      .handle(Request(HttpMethod.Get, "/users")) match
      case Response(HttpStatus.NotFound, _, _, _, _) =>
      case resp =>  fail(s"expected not found: $resp")

    server
      .handle(Request(HttpMethod.Get, "/user", "ping")) match
      case Response(HttpStatus.OK, "pong", _, _, _) =>
      case resp =>  fail(s"expected pong: $resp")
  }




