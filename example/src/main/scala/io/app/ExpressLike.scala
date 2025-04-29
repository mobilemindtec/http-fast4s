package io.app

import io.http.fast4s.api.*

object ExpressLike:

  fast.get("/") {
    case req => Response.ok("hello, world!")
  }

  fast.get("/user") {
    case (req: Request) =>
      Response.ok(s"hello, ${req.query.str("name").getOrElse("anonymous")}!")
  }

  fast.get("/err") {
    case req => throw Exception("err")
  }

  fast.recover {
    case (req, err) => Response.ok("recovered!")
  }

  fast.intercept(404) {
    case (req, resp) => Response.ok("404")
  }

  fast.enter(Get, "/.*") {
    case req =>
      println(s"ns enter ${req.target}")
      req
  }

  fast.leave(Get, "/.*") {
    case (req, resp) =>
      println(s"ns leave ${req.target}")
      resp
  }

  def serve = fast.serve()
