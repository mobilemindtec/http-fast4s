
import scalanative.unsafe.*



@extern
object http_extern {
  type HeaderC = Ptr[CStruct2[CString, CString]]
  type RequestC = Ptr[CStruct2[CString, Ptr[HeaderC]]]
  // status, headers, body, contentType
  type ResponseC = Ptr[CStruct4[CInt, Ptr[HeaderC], CString, CString]]
}
@extern
object beast_server {


  import http_extern._

  def run(hostname: CString,
          port: CUnsignedShort,
          maxThread: CUnsignedShort,
          handlerPtr: Ptr[Byte]): CInt = extern

  @name("create_http_handler")
  def createHttpHandler(): Ptr[Byte] = extern

  @name("add_http_get_handler")
  def addHttpGetHandler(handlerPtr: Ptr[Byte], cb: CFuncPtr1[RequestC, ResponseC]): Unit = extern

  @name("create_http_handler_async")
  def createHttpAsyncHandler(): Ptr[Byte] = extern

  @name("add_http_get_async_handler")
  def addHttpGetAsyncHandler(handlerPtr: Ptr[Byte], cb: CFuncPtr2[Unit, RequestC, CFuncPtr2[Unit, RequestC, ResponseC]]): Unit = extern

  @name("create_response")
  def createResponse(body: CString, contentType: CString): ResponseC = extern
}



object Main {
  import beast_server._

  def main(args: Array[String]): Unit =

    val handler = createHttpHandler()
    addHttpGetHandler(
      handler,
      CFuncPtr1.fromScalaFunction(_ => createResponse(c"ScalaNative rocks!!", c"text/plain")))

    val port = 8181.asInstanceOf[CUnsignedShort]
    val threads = 1.asInstanceOf[CUnsignedShort]

    run(c"0.0.0.0", port, threads, handler)
    ()
}
