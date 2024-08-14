package br.com.mobilemind.beast

import br.com.mobilemind.beast.HttpServerBase.threadStart
import br.com.mobilemind.beast.beast_server.*
import br.com.mobilemind.micro.routing.router.{Method, Router}

import scala.collection.mutable
import scala.scalanative.unsafe
import scala.scalanative.unsafe.{CFuncPtr, CFuncPtr2, CFuncPtr3, CInt, Ptr, Zone}
import scala.scalanative.unsigned.UnsignedRichInt



trait HttpServerAsync[Req <: HttpRequest, Resp <: HttpResponse](val host: String,
                                                                val port: Int,
                                                                val workers: Int = 1)
  extends HttpServerBase[Req, Resp]:

  import converters.*

  def handle(req: BeastRequestPtr, handlerPtr: Ptr[Byte]): Unit =
    val resp = dispatch(req)
    Zone:
      val beastCallback = CFuncPtr.fromPtr[BeastHandlerCallback](handlerPtr)
      beastCallback(req, resp.response())

  override def run: Int =

    Zone:
      runBeastAsync(
        unsafe.toCString(host),
        port.toUShort,
        workers.toUShort,
        CFuncPtr3.fromScalaFunction(threadStart),
        CFuncPtr2.fromScalaFunction(handle))
