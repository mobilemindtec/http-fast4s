package io.http.fast4s.app

import io.http.fast4s.beast.{HttpServerAsync, HttpServerSync}
import io.http.fast4s.core.{HttpServer, Interceptor, NSEnter, NSLeave, Recover}
import io.http.fast4s.types.{RawRequest, Request, Response, toMethod, HttpMethod}
import io.micro.router.api.*

import scala.collection.mutable

case object Fast4s:

  private type ServerResult = RequestBuilder[Request, RawRequest] ?=> Int

  private val _routes = mutable.ListBuffer[RouteEntry[Request, Response]]()

  // ns middlewares
  private val _leave = mutable.ListBuffer[NSLeave]()

  private val _enter = mutable.ListBuffer[NSEnter]()

  private val _interceptors = mutable.Map[Int, Interceptor]()

  private var _recover: Option[Recover] = None

  private def register(route: RouteEntry[Request, Response]) =
    _routes.addOne(route)
    route

  private def buildAsyncServer(host: String,
                       port: Int,
                       threads: Int,
                       routes: Seq[RouteEntry[Request, Response]]): RequestBuilder[Request, RawRequest] ?=> HttpServer =
    val server = new HttpServerAsync(host, port, threads):
      def router: Router[Request, Response, RawRequest] = Router(routes *)
      def recover: Option[Recover] = _recover
      def interceptors: Map[Int, Interceptor] = _interceptors.toMap
      def leave: Seq[NSLeave] = _leave.toSeq
      def enter: Seq[NSEnter] = _enter.toSeq
    server.build

  private def runServerAsync(host: String,
                             port: Int,
                             threads: Int,
                             routes: Seq[RouteEntry[Request, Response]]): ServerResult =
    buildAsyncServer(host, port, threads, routes)
      .serve

  private def buildServer(host: String,
                        port: Int,
                        threads: Int,
                        routes: Seq[RouteEntry[Request, Response]]): RequestBuilder[Request, RawRequest] ?=> HttpServer =
    val server = new HttpServerSync(host, port, threads):
      def router: Router[Request, Response, RawRequest] = Router(routes *)
      def recover: Option[Recover] = _recover
      def interceptors: Map[Int, Interceptor] = _interceptors.toMap
      def leave: Seq[NSLeave] = _leave.toSeq
      def enter: Seq[NSEnter] = _enter.toSeq
    server.build

  private def runServer(host: String,
                        port: Int,
                        threads: Int,
                        routes: Seq[RouteEntry[Request, Response]]): ServerResult =
      buildServer(host, port, threads, routes)
        .serve

  object fast:

    import extra.given_ReqBuilder

    def get(path: String, c: Controller[Request, Response]): RouteEntry[Request, Response] =
      route(GET, path, c) |> register

    def get(path: String)(f: Handler[Request, Response]): RouteEntry[Request, Response] =
      route(GET, path)(f) |> register

    def get(path: String)(f: Dispatcher[Request, Response]): RouteEntry[Request, Response] =
      route(GET, path)(f) |> register

    def head(path: String, c: Controller[Request, Response]): RouteEntry[Request, Response] =
      route(HEAD, path, c) |> register

    def head(path: String)(f: Handler[Request, Response]): RouteEntry[Request, Response] =
      route(HEAD, path)(f) |> register

    def head(path: String)(f: Dispatcher[Request, Response]): RouteEntry[Request, Response] =
      route(HEAD, path)(f) |> register

    def options(path: String, c: Controller[Request, Response]): RouteEntry[Request, Response] =
      route(OPTIONS, path, c) |> register

    def options(path: String)(f: Handler[Request, Response]): RouteEntry[Request, Response] =
      route(OPTIONS, path)(f) |> register

    def options(path: String)(f: Dispatcher[Request, Response]): RouteEntry[Request, Response] =
      route(OPTIONS, path)(f) |> register

    def post(path: String, c: Controller[Request, Response]): RouteEntry[Request, Response] =
      route(POST, path, c) |> register

    def post(path: String)(f: Handler[Request, Response]): RouteEntry[Request, Response] =
      route(POST, path)(f) |> register

    def post(path: String)(f: Dispatcher[Request, Response]): RouteEntry[Request, Response] =
      route(POST, path)(f) |> register

    def put(path: String, c: Controller[Request, Response]): RouteEntry[Request, Response] =
      route(PUT, path, c) |> register

    def put(path: String)(f: Handler[Request, Response]): RouteEntry[Request, Response] =
      route(PUT, path)(f) |> register

    def put(path: String)(f: Dispatcher[Request, Response]): RouteEntry[Request, Response] =
      route(PUT, path)(f) |> register

    def delete(path: String, c: Controller[Request, Response]): RouteEntry[Request, Response] =
      route(DELETE, path, c) |> register

    def delete(path: String)(f: Handler[Request, Response]): RouteEntry[Request, Response] =
      route(DELETE, path)(f) |> register

    def delete(path: String)(f: Dispatcher[Request, Response]): RouteEntry[Request, Response] =
      route(DELETE, path)(f) |> register

    def patch(path: String, c: Controller[Request, Response]): RouteEntry[Request, Response] =
      route(PATCH, path, c) |> register

    def patch(path: String)(f: Handler[Request, Response]): RouteEntry[Request, Response] =
      route(PATCH, path)(f) |> register

    def patch(path: String)(f: Dispatcher[Request, Response]): RouteEntry[Request, Response] =
      route(PATCH, path)(f) |> register

    def connect(path: String, c: Controller[Request, Response]): RouteEntry[Request, Response] =
      route(CONNECT, path, c) |> register

    def connect(path: String)(f: Handler[Request, Response]): RouteEntry[Request, Response] =
      route(CONNECT, path)(f) |> register

    def connect(path: String)(f: Dispatcher[Request, Response]): RouteEntry[Request, Response] =
      route(CONNECT, path)(f) |> register

    def any(path: String, c: Controller[Request, Response]): RouteEntry[Request, Response] =
      route(ANY, path, c) |> register

    def any(path: String)(f: Handler[Request, Response]): RouteEntry[Request, Response] =
      route(ANY, path)(f) |> register

    def any(path: String)(f: Dispatcher[Request, Response]): RouteEntry[Request, Response] =
      route(ANY, path)(f) |> register

    def routes(items: RouteEntry[Request, Response]*): Unit =
      _routes.addAll(items)

    def recover(f: Recover): Unit =
      _recover = Some(f)
      ()

    def intercept(status: Int)(f: Interceptor): Unit =
      _interceptors.addOne(status -> f)

    def enter(method: HttpMethod, path: String)(f: MiddlewareEnter[Request, Response]): Unit =
      ns(path, Enter(method.toMethod :: Nil, f))

    def leave(method: HttpMethod, path: String)(f: MiddlewareLeave[Request, Response]): Unit =
      ns(path, Leave(method.toMethod :: Nil, f))

    def ns(path: String, m: Leave[Request, Response] | Enter[Request, Response]): Unit =
      m match
        case af: Leave[Request, Response] =>
          _leave.addOne((path, af))
        case bf: Enter[Request, Response] =>
          _enter.addOne((path, bf))


    def serveAsync(hostname: String = "0.0.0.0", port: Int = 3000, threads: Int = 1): Int =
      runServerAsync(
        hostname,
        port,
        threads,
        _routes.toSeq)

    def serve(hostname: String = "0.0.0.0", port: Int = 3000, threads: Int = 1): Int =
      runServer(hostname, port, threads, _routes.toSeq)

    object server:

      def createAsync(hostname: String = "0.0.0.0", port: Int = 3000, threads: Int = 1): HttpServer =
        Fast4s.buildAsyncServer(hostname, port, threads, _routes.toSeq)

      def create(hostname: String = "0.0.0.0", port: Int = 3000, threads: Int = 1): HttpServer =
        Fast4s.buildServer(hostname, port, threads, _routes.toSeq)
