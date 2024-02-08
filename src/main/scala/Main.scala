package httpserver

import scala.scalanative.unsafe
import scala.scalanative.unsafe.CFuncPtr.toPtr
import scalanative.unsafe.*
import scalanative.unsigned.UnsignedRichInt



@extern
object beast_server {


  // name, value
  type BeastHeader = CStruct2[CString, CString]
  type BeastHeaderPtr = Ptr[BeastHeader]

  // verb, target, body, headers
  type BeastRequest = CStruct4[CString, CString, CString, BeastHeaderPtr]
  type BeastRequestPtr = Ptr[BeastRequest]
  // status, headers, body, contentType

  // status code, body, content type, content size, headers
  type BeastResponse = CStruct5[CInt, CString, CString, CInt, BeastHeaderPtr]
  type BeastResponsePtr = Ptr[BeastResponse]

  type BeastHandlerCallback = CFuncPtr2[BeastRequestPtr, BeastResponsePtr, Unit]
  type BeastHttpHandlerSync = CFuncPtr1[BeastRequestPtr, BeastResponsePtr]
  type BeastHttpHandlerAsync = CFuncPtr2[BeastRequestPtr, BeastHandlerCallback, Unit]


  @name("run_sync")
  def runBeastSync(hostname: CString,
                   port: CUnsignedShort,
                   maxThread: CUnsignedShort,
                   handler: BeastHttpHandlerSync): CInt = extern

  @name("run_async")
  def runBeastAsync(hostname: CString,
                    port: CUnsignedShort,
                    maxThread: CUnsignedShort,
                    handler: CFuncPtr): CInt = extern


}

extension (cs: CString)
  def string = fromCString(cs)

object Main {
  import beast_server._

  def newResponse(body: CString): BeastResponsePtr =
    val ptr = unsafe.stackalloc[BeastResponse]()
    ptr._1 = 200
    ptr._2 = body
    ptr._3 = c"text/plain"
    ptr

  def handlerSync(req: BeastRequestPtr): BeastResponsePtr =
    newResponse(c"Scala rock's!!")

  def handlerAsync(req: BeastRequestPtr, handlerPtr: Ptr[Byte]): Unit =

    println(s"${req._1.string} ${req._2.string}: ${req._3}")

    val handler = CFuncPtr.fromPtr[BeastHandlerCallback](handlerPtr)
    val resp = newResponse(c"Scala rock's async!!")
    handler(req, resp)


  def main(args: Array[String]): Unit =
    val port: CInt = 8181
    val threads: CInt = 1
    println("start start server")
    //runSync(c"0.0.0.0", port.toUShort, threads.toUShort, CFuncPtr1.fromScalaFunction(handlerSync))
    runBeastAsync(c"0.0.0.0", port.toUShort, threads.toUShort, CFuncPtr2.fromScalaFunction(handlerAsync))
    ()
}
