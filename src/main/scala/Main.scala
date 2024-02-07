package httpserver

import scala.scalanative.unsafe
import scala.scalanative.unsafe.CFuncPtr.toPtr
import scalanative.unsafe.*
import scalanative.unsigned.UnsignedRichInt



@extern
object http_extern {
  type Header = CStruct2[CString, CString]
  type HeaderPtr = Ptr[Header]

  type Request = CStruct2[CString, HeaderPtr]
  type RequestPtr = Ptr[Request]
  // status, headers, body, contentType

  type Response = CStruct4[CInt, HeaderPtr, CString, CString]
  type ResponsePtr = Ptr[Response]

  type HttpHandler = CFuncPtr2[RequestPtr, ResponsePtr, Unit]
  type HttpHandlerSync = CFuncPtr1[RequestPtr, ResponsePtr]
  type HttpHandlerAsync = CFuncPtr2[RequestPtr, HttpHandler, Unit]

}
@extern
object beast_server {


  import http_extern._

  @name("run_sync")
  def runSync(hostname: CString,
          port: CUnsignedShort,
          maxThread: CUnsignedShort,
          handler: HttpHandlerSync): CInt = extern

  @name("run_async")
  def runAsync(hostname: CString,
              port: CUnsignedShort,
              maxThread: CUnsignedShort,
              handler: CFuncPtr): CInt = extern


}

extension (cs: CString)
  def string = fromCString(cs)

object Main {
  import beast_server._
  import http_extern._

  def newResponse(body: CString): ResponsePtr =
    val ptr = unsafe.stackalloc[Response]()
    ptr._1 = 200
    ptr._3 = body
    ptr._4 = c"text/plain"
    ptr

  def handlerSync(req: RequestPtr): ResponsePtr =
    newResponse(c"Scala rock's!!")

  def handlerAsync(req: RequestPtr, handlerPtr: Ptr[Byte]): Unit =
    val handler = CFuncPtr.fromPtr[HttpHandler](handlerPtr)
    val resp = newResponse(c"Scala rock's async!!")
    handler(req, resp)


  def main(args: Array[String]): Unit =
    val port: CInt = 8181
    val threads: CInt = 1
    println("start start server")
    //runSync(c"0.0.0.0", port.toUShort, threads.toUShort, CFuncPtr1.fromScalaFunction(handlerSync))
    runAsync(c"0.0.0.0", port.toUShort, threads.toUShort, CFuncPtr2.fromScalaFunction(handlerAsync))
    ()
}
