package io.http.fast4s

import io.http.fast4s.bindings.structs.ThreadInit
import io.http.fast4s.types.*
import io.micro.router.core.|>
import io.micro.router.{Leave, Enter, Router}

import scala.language.experimental.namedTuples
import scala.annotation.tailrec
import scala.compiletime.uninitialized
import scala.scalanative.unsafe.{CInt, Ptr}
import scala.util.matching.Regex


type Interceptor = (Request, Response) => Response
type Recover = (Request, Throwable) => Response

type NSLeave = (path: String, after: Leave[Request, Response])
type NSEnter = (path: String, before: Enter[Request, Response])

trait HttpServer:

  def router: Router[Request, Response, RawRequest]
  def recover: Option[Recover]
  def interceptors: Map[Int, Interceptor]
  def after: Seq[NSLeave]
  def before: Seq[NSEnter]
  def run: Int
  def serve(): Int =
    run

  def build(): HttpServer =
    HttpServer.httpServer = this
    HttpServer.recover = recover
    HttpServer.interceptors = interceptors
    HttpServer.after = after
    HttpServer.before = before
    this

  def handle(req: Request): Response = HttpServer.handle(req)

private[fast4s] object HttpServer:

  var httpServer: HttpServer = uninitialized
  var recover: Option[Recover] = None
  var interceptors: Map[Int, Interceptor] = Map()
  var after: Seq[NSLeave] = Nil
  var before: Seq[NSEnter] = Nil

  inline def applyEnter(request: Request): Request | Response =
    @tailrec
    def each(req: Request, items: Seq[NSEnter]): Request | Response =
      items match
        case Nil => req
        case x :: xs =>
          val res =
            if Regex(x.path).matches(req.target)
            then
              httpServer
                .router
                .applyEnter(req.method.toMethod, req, Some(x.before))
            else req
          res match
            case  r: Request => each(r, xs)
            case _ => res
    each(request, httpServer.before)

  inline def applyLeave(request: Request, response: Response): Response =
    @tailrec
    def each(resp: Response, items: Seq[NSLeave]): Response =
      items match
        case Nil => resp
        case x :: xs =>
          val res =
            if Regex(x.path).matches(request.target)
            then
              httpServer
                .router
                .applyLeave(request.method.toMethod, request, resp, Some(x.after))
            else resp
          each(res, xs)
    each(response, httpServer.after)

  inline def applyInterceptors(request: Request)(response: Response): Response =
    @tailrec
    def each(resp: Response, items: Seq[Interceptor]): Response =
      items match
        case Nil => resp
        case x :: xs => each(x.apply(request, resp), xs)
    val its = interceptors
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
          HttpServer
            .recover
            .map { f =>
              f(request, err)
            }.getOrElse(Response.serverError().asInstanceOf[Response])

  inline def dispatch(request: Request): Response =
    val target = request.target
    val method = request.method.toMethod
    val extra = RawRequest(
      request.body,
      request.rawBody,
      request.headers
    )
    
    httpServer
      .router
      .dispatch(method, target, extra) match
        case Some(resp) => resp
        case _ => Response.notFound("Not Found").asInstanceOf[Response]

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