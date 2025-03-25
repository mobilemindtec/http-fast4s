package app

import io.http.fast4s.app.Fast4s.*
import io.http.fast4s.types.HttpMethod.Get
import io.http.fast4s.types.{Request, Response}

object AppServer:

  app.get("/") {
    case req => Response.ok("hello, world!")
  }

  app.get("/user") {
    case (req: Request) =>
      Response.ok(s"hello, ${req.query.str("name").getOrElse("anonymous")}!")
  }

  app.get("/err") {
    case req => throw Exception("err")
  }

  app.recover {
    case (req, err) => Response.ok("recovered!")
  }

  app.intercept(404) {
    case (req, resp) => Response.ok("404")
  }

  app.enter(Get, "/.*") {
    case req =>
      println(s"ns enter ${req.target}")
      req
  }

  app.leave(Get, "/.*") {
    case (req, resp) =>
      println(s"ns leave ${req.target}")
      resp
  }

  def serve =
    app.serve()

@main def main(args: String*): Int =
  AppServer.serve