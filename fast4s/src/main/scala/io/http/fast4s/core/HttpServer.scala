package io.http.fast4s.core

import io.http.fast4s.types.{Request, Response, RawRequest, toMethod}
import io.http.fast4s.beast.structs.ThreadInit
import io.micro.router.api.*

import scala.annotation.tailrec
import scala.compiletime.uninitialized
import scala.scalanative.unsafe.{CInt, Ptr}
import scala.util.matching.Regex


type Interceptor = (Request, Response) => Response
type Recover = (Request, Throwable) => Response

type NSLeave = (path: String, leave: Leave[Request, Response])
type NSEnter = (path: String, enter: Enter[Request, Response])

trait HttpServer:

  def router: Router[Request, Response, RawRequest]
  def recover: Option[Recover]
  def interceptors: Map[Int, Interceptor]
  def leave: Seq[NSLeave]
  def enter: Seq[NSEnter]
  def run: Int
  def serve: Int = run

  def build: HttpServer =
    HttpServer.configs = this
    this

private[fast4s] object HttpServer:

  private var configs: HttpServer = uninitialized

  inline def applyEnter(request: Request): Request | Response =
    @tailrec
    def each(req: Request, items: Seq[NSEnter]): Request | Response =
      items match
        case Nil => req
        case x :: xs =>
          val res =
            if Regex(x.path).matches(req.target)
            then
              configs
                .router
                .applyEnter(req.method.toMethod, req, Some(x.enter))
            else req
          res match
            case  r: Request => each(r, xs)
            case _ => res
    each(request, configs.enter)

  inline def applyLeave(request: Request, response: Response): Response =
    @tailrec
    def each(resp: Response, items: Seq[NSLeave]): Response =
      items match
        case Nil => resp
        case x :: xs =>
          val res =
            if Regex(x.path).matches(request.target)
            then
              configs
                .router
                .applyLeave(request.method.toMethod, request, resp, Some(x.leave))
            else resp
          each(res, xs)
    each(response, configs.leave)

  inline def applyInterceptors(request: Request)(response: Response): Response =
    @tailrec
    def each(resp: Response, items: Seq[Interceptor]): Response =
      items match
        case Nil => resp
        case x :: xs => each(x.apply(request, resp), xs)
    val its = configs.interceptors
      .filter(_._1 == response.status.code)
      .values
      .toSeq
    each(response, its)

  inline def handle(request: Request): Response =
      try
        val resp =
          applyEnter(request) match
            case r: Request => dispatch(r)
            case r: Response => r
        applyLeave(request, resp) |> applyInterceptors(request)
      catch
        case err: Throwable =>
          println("Error during http server handle: " + err)
          configs
            .recover
            .map { f =>
              f(request, err)
            }.getOrElse(Response.serverError())

  inline def dispatch(request: Request): Response =
    val target = request.target
    val method = request.method.toMethod
    val extra = RawRequest(
      request.body,
      request.rawBody,
      request.headers
    )

    configs
      .router
      .dispatch(method, target, extra) match
        case Some(resp) => resp
        case _ => Response.notFound("Not Found")

  def threadStart(init: ThreadInit, workers: CInt, ptr: Ptr[Byte]): Unit =
    val threads =
      for _ <- 0 until workers yield
        new Thread:
          override def run(): Unit =
            init(ptr)

    for t <- threads do
      t.start()

    for t <- threads do
      t.join()