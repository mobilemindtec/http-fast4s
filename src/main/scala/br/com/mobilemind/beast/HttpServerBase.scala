package br.com.mobilemind.beast

import br.com.mobilemind.beast.beast_server.{BeastRequestPtr, ThreadInit}
import br.com.mobilemind.micro.routing.router.Router

import scala.scalanative.unsafe.{CInt, Ptr}

trait HttpServerBase[Req <: HttpRequest, Resp <: HttpResponse]:

  import converters.*

  def router: Router[Req, Resp, ReqExtra]

  def fail(request: Req, status: HttpStatus): Resp

  def dispatch(req: BeastRequestPtr): Resp =
    val request = req.request().asInstanceOf[Req]
    val target = request.target
    val method = request.method.toMethod
    val extra = ReqExtra(
      request.body,
      request.bodyRaw,
      request.headers
    )
    
    router.dispatch(method, target, extra) match
      case Some(resp) =>
        resp
      case _ =>
        fail(request, HttpStatus.NotFound)

  def run: Int


object HttpServerBase:

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