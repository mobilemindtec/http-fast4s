import io.http.fast4s.HttpServerBuilder.newBuilder
import io.http.fast4s.app.extra.given_ReqBuilder
import io.http.fast4s.types.{Request, Response}
import io.micro.router.{Enter, RouteEntry}
import io.micro.router.Router.{enter, route}
import io.micro.router.core.Method.Get
import io.micro.router.core.Path.root

object AppServer:

  type RouteEnter = Enter[Request, Response]
  type Route = RouteEntry[Request, Response]

  def server =

    val logger: RouteEnter = enter(Get):
      req =>
        println(s"enter in ${req.target}")
        req

    val home: Route = route(Get, root):
      req => Response.ok("alive!")

    val ping: Route = route(Get, root / "ping"):
      case _ => Response.ok("pong")

    newBuilder
      .withRoutes(
        logger ++ home,
        logger ++ ping
      )
      .build


@main def main(args: String*): Int =
  AppServer.server.serve

