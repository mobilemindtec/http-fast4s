package io.http.fast4s.bindings

import io.http.fast4s.HttpServer
import io.http.fast4s.HttpServer.{dispatch, threadStart}
import io.http.fast4s.bindings.structs.*
import io.http.fast4s.bindings.BeastServer.*
import io.http.fast4s.bindings.conv.{ptr, request}
import io.http.fast4s.types.{Request, Response}

import scala.scalanative.unsafe
import scala.scalanative.unsafe.{CFuncPtr, CFuncPtr2, CFuncPtr3, Ptr, Zone}
import scala.scalanative.unsigned.UnsignedRichInt

object HttpServerAsync:

  def handle(req: Ptr[BeastRequest], handlerPtr: Ptr[Byte]): Unit =
    val resp = HttpServer.handle(req.request())
    Zone:
      val beastCallback = CFuncPtr.fromPtr[BeastHandlerCallback](handlerPtr)
      beastCallback(req, resp.ptr())

trait HttpServerAsync(val host: String,
                      val port: Int,
                      val workers: Int = 1) extends HttpServer:

  override def run: Int =
    Zone:
      runBeastAsync(
        unsafe.toCString(host),
        port.toUShort,
        workers.toUShort,
        CFuncPtr3.fromScalaFunction(threadStart),
        CFuncPtr2.fromScalaFunction(HttpServerAsync.handle))
