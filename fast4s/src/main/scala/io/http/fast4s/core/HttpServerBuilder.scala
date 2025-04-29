package io.http.fast4s.core

import io.http.fast4s.app.extra
import io.http.fast4s.beast.{HttpServerAsync, HttpServerSync}
import io.http.fast4s.types.{RawRequest, Request, Response}
import io.micro.router.api.{RequestBuilder, RouteEntry, Router}

type ReqBuilder = RequestBuilder[Request, RawRequest]

class HttpServerBuilder(using ReqBuilder):

  private val cfg: HttpServerConfigs = HttpServerConfigs()

  def withPort(port: Int): HttpServerBuilder =
    cfg.port = port
    this

  def withHost(host: String): HttpServerBuilder =
    cfg.host = host
    this

  def withWorkers(workers: Int): HttpServerBuilder =
    cfg.workers = workers
    this

  def withRoutes(routes: RouteEntry[Request, Response]*): HttpServerBuilder =
    cfg.routes ++= routes
    this

  def withRoute(route: => RouteEntry[Request, Response]): HttpServerBuilder =
    cfg.routes += route
    this

  def withRecover(recover: Recover): HttpServerBuilder =
    cfg.recover = Some(recover)
    this

  def withInterceptor(status: Int)(interceptor: => Interceptor): HttpServerBuilder =
    cfg.interceptors += (status -> interceptor)
    this

  def withLeave(leave: Seq[NSLeave]): HttpServerBuilder =
    cfg.leave ++= leave
    this

  def withLeave(leave: => NSLeave): HttpServerBuilder =
    cfg.leave += leave
    this

  def withEnter(enter: Seq[NSEnter]): HttpServerBuilder =
    cfg.enter ++= enter
    this

  def withEnter(enter: => NSEnter): HttpServerBuilder =
    cfg.enter += enter
    this

  def async: HttpServerBuilder =
    cfg.async = true
    this

  def build: HttpServer =
    if cfg.async then buildAsync else buildSync

  private def buildSync: HttpServer =
    val server = new HttpServerSync(cfg.host, cfg.port, cfg.workers):
      def router: Router[Request, Response, RawRequest] = Router(cfg.routes.toSeq *)

      def recover: Option[Recover] = cfg.recover

      def interceptors: Map[Int, Interceptor] = cfg.interceptors.toMap

      def leave: Seq[NSLeave] = cfg.leave.toSeq

      def enter: Seq[NSEnter] = cfg.enter.toSeq

    server.build

  private def buildAsync: HttpServer =
    val server = new HttpServerAsync(cfg.host, cfg.port, cfg.workers):
      def router: Router[Request, Response, RawRequest] = Router(cfg.routes.toSeq *)

      def recover: Option[Recover] = cfg.recover

      def interceptors: Map[Int, Interceptor] = cfg.interceptors.toMap

      def leave: Seq[NSLeave] = cfg.leave.toSeq

      def enter: Seq[NSEnter] = cfg.enter.toSeq

    server.build

object HttpServerBuilder:
  def newHttpServerBuilder: ReqBuilder ?=> HttpServerBuilder = new HttpServerBuilder()