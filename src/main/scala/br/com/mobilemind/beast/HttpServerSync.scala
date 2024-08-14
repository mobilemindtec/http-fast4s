package br.com.mobilemind.beast

import br.com.mobilemind.beast.HttpServerBase.threadStart
import br.com.mobilemind.beast.beast_server.*
import br.com.mobilemind.micro.routing.router.Router

import scala.scalanative.unsafe
import scala.scalanative.unsafe.{CFuncPtr1, CFuncPtr2, CFuncPtr3, CInt, Ptr, Zone}
import scala.scalanative.unsigned.UnsignedRichInt

trait HttpServerSync[Req <: HttpRequest, Resp <: HttpResponse](val host: String,
                                                               val port: Int,
                                                               val workers: Int = 1)
  extends HttpServerBase[Req, Resp]:

  import converters.*

  private def handle(req: BeastRequestPtr): BeastResponsePtr =
    val resp = dispatch(req)
    Zone:
      resp.response()

  override def run: Int =
    Zone:
      runBeastSync(
        unsafe.toCString(host),
        port.toUShort,
        workers.toUShort,
        CFuncPtr3.fromScalaFunction(threadStart),
        CFuncPtr1.fromScalaFunction(handle))
