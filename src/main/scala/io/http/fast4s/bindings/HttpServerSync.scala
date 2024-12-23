package io.http.fast4s.bindings

import io.http.fast4s.HttpServer.{dispatch, threadStart}
import io.http.fast4s.bindings.structs.*
import io.http.fast4s.bindings.EasyBeastInterop.*
import io.http.fast4s.types.{Request, Response}
import io.http.fast4s.bindings.conv.{request, ptr}
import io.http.fast4s.HttpServer

import scala.scalanative.unsafe
import scala.scalanative.unsafe.{CFuncPtr1, CFuncPtr3, Ptr, Zone}
import scala.scalanative.unsigned.UnsignedRichInt

object HttpServerSync:

  private def handle(req: Ptr[BeastRequest]): Ptr[BeastResponse] =
    val resp = HttpServer.handle(req.request())
    Zone:
      resp.ptr()

trait HttpServerSync(val host: String,
                     val port: Int,
                     val workers: Int = 1) extends HttpServer:

  override def run: Int =
    Zone:
      runBeastSync(
        unsafe.toCString(host),
        port.toUShort,
        workers.toUShort,
        CFuncPtr3.fromScalaFunction(threadStart),
        CFuncPtr1.fromScalaFunction(HttpServerSync.handle))
