package httpserver

import scala.collection.mutable
import scala.scalanative.unsafe
import scala.scalanative.unsafe.CFuncPtr.toPtr
import scalanative.unsafe.*
import scalanative.unsigned.UnsignedRichInt



@extern
object beast_server:


  // name, value
  type BeastHeader = CStruct2[CString, CString]
  type BeastHeaderPtr = Ptr[BeastHeader]

  // header start pointer, size
  type BeastHeaders = CStruct2[BeastHeaderPtr, CInt]
  type BeastHeadersPtr = Ptr[BeastHeaders]

  // body {str, body raw, int}
  type BeastBody = CStruct3[CString, Ptr[Byte], CInt]
  type BeastBodyPtr = Ptr[BeastBody]

  // verb, target, content type, {body str, body bytes, size} , {[{name, value], size}
  type BeastRequest = CStruct5[CString, CString, CString, BeastBodyPtr, BeastHeadersPtr]
  type BeastRequestPtr = Ptr[BeastRequest]
  // status, headers, body, contentType

  // status {code, content type, {body str, body bytes, size}, {[{name, value], size}}
  type BeastResponse = CStruct4[CInt, CString, BeastBodyPtr, BeastHeadersPtr]
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


object beast:

  import beast_server._

  extension (cs: CString)
    def string = fromCString(cs)

  type Headers = Map[String, String]

  enum HttpMethod(val verb: String):
    case Head extends HttpMethod("HEAD")
    case Options extends HttpMethod("OPTIONS")
    case Patch extends HttpMethod("PATCH")
    case Get extends HttpMethod("GET")
    case Post extends HttpMethod("POST")
    case Put extends HttpMethod("PUT")
    case Delete extends HttpMethod("DELETE")

  def toHttpMethod(verb: String): HttpMethod =
    HttpMethod.values.find(m => m.verb.equals(verb)).getOrElse(HttpMethod.Get)

  trait HttpRequest(val target: String,
                    val method: HttpMethod,
                    val body: Option[String] = None,
                    val bodyRaw: Option[Seq[Byte]] = None,
                    val contentType: String,
                    val headers: Headers = Map())

  trait HttpResponse(val statusCode: Int,
                     val body: Option[String] = None,
                     val bodyRaw: Option[Seq[Byte]] = None,
                     val contentType: String,
                     val headers: Headers = Map()):
    def hasBody: Boolean = hasBodyStr || hasBodyRaw

    def hasBodyStr: Boolean = body.nonEmpty
    def hasBodyRaw: Boolean = bodyRaw.nonEmpty

    def bodySize: Int =
      body.map(_.length).getOrElse(bodyRaw.map(_.size).getOrElse(0))


  sealed trait HttpServerBase:
    def run: Int

  sealed trait HttpServerBaseAsync[Req <: HttpRequest, Resp <: HttpResponse] extends HttpServerBase:
    def handle(req: Req, cb: Resp => Unit): Unit

  sealed trait HttpServerBaseSync[Req <: HttpRequest, Resp <: HttpResponse] extends HttpServerBase:
    def handle(req: Req): Resp

  class Request(override val target: String,
                override val method: HttpMethod,
                override val contentType: String,
                override val body: Option[String] = None,
                override val bodyRaw: Option[Seq[Byte]] = None,
                override val headers: Headers = Map())
    extends HttpRequest(target, method, body, bodyRaw, contentType, headers)

  class Response(override val statusCode: Int,
                 override val body: Option[String] = None,
                 override val bodyRaw: Option[Seq[Byte]] = None,
                 override val contentType: String = "",
                 override val headers: Headers = Map())
    extends HttpResponse(statusCode, body, bodyRaw, contentType, headers)

  object BeastConverters:

      // request struct {verb, target, content type, {body str, body bytes, size} , {[{name, value], size}}
      def toRequest(req: BeastRequestPtr): Request =

        var bodyRaw: Option[Seq[Byte]] = None
        var body: Option[String] = None
        val headers = mutable.Map[String, String]()

        if req._4 != null then
          val bodyPtr = req._4
          val bodyStr = bodyPtr._1
          var bodyBytes = bodyPtr._2
          val bodyLen = bodyPtr._3

          if(bodyStr != null)
            body = Some(bodyStr.string)

          if bodyBytes != null then
            val raw = mutable.Seq.empty[Byte]
            for i <- 0 until bodyLen do
              raw(i) = !bodyBytes
              bodyBytes += 1
            bodyRaw = Some(raw.toSeq)

        val headersPtr = req._5
        if headersPtr != null then
          var headerPtr = headersPtr._1;
          val headersLen = headersPtr._2
          for _ <- 0 until headersLen do
            headers(headerPtr._1.string) = headerPtr._2.string
            headerPtr += 1

        Request(
          method = toHttpMethod(req._1.string),
          target = req._2.string,
          contentType = req._3.string,
          body = body,
          bodyRaw = bodyRaw,
          headers = headers.toMap
        )

    // beast struct {status code, content type, {body str, body bytes, size}, {[{name, value], size}}
    def toResponsePtr(response: HttpResponse)(using Zone): BeastResponsePtr =

      val resp = unsafe.stackalloc[BeastResponse]()
      resp._1 = response.statusCode
      resp._2 = unsafe.toCString(response.contentType)

      if response.hasBody then

        val body = unsafe.stackalloc[BeastBody]()
        body._3 = response.bodySize

        if response.hasBodyStr then
          body._1 = unsafe.toCString(response.body.get)
        else
          val bsize = response.bodySize
          var bytes = unsafe.stackalloc[Byte](bsize.toUInt)
          for b <- response.bodyRaw.get  do
            bytes += 1
          body._2 = bytes
        resp._3 =  body

      if response.headers.nonEmpty then
        val hsize = response.headers.size
        val headers = unsafe.stackalloc[BeastHeaders]()
        headers._1 = unsafe.stackalloc[BeastHeader](hsize.toUInt)
        headers._2 = hsize

        for (h <- response.headers; i <- 0 until hsize) do
            val header = unsafe.stackalloc[BeastHeader]()
            header._1 = unsafe.toCString(h._1)
            header._2 = unsafe.toCString(h._2)
            headers._1(i) = header

        resp._4 = headers
      resp

  trait HttpServerAsync[Req <: HttpRequest, Resp <: HttpResponse](val host: String,
                                                                  val port: Int,
                                                                  val workers: Int = 1)
    extends HttpServerBaseAsync[Req, Resp]:

    import BeastConverters._

    def handlerAsync(req: BeastRequestPtr, handlerPtr: Ptr[Byte]): Unit =
      val request = toRequest(req).asInstanceOf[Req]
      handle(request, { resp =>
        Zone:
          implicit z =>
            val beastCallback = CFuncPtr.fromPtr[BeastHandlerCallback](handlerPtr)
            beastCallback(req, toResponsePtr(resp))
      })

    override def run: Int =
      Zone:
        implicit z =>
          runBeastAsync(
            unsafe.toCString(host),
            port.toUShort,
            workers.toUShort,
            CFuncPtr2.fromScalaFunction(handlerAsync))

  trait HttpServerSync[Req <: HttpRequest, Resp <: HttpResponse](val host: String,
                                                                 val port: Int,
                                                                 val workers: Int = 1)
    extends HttpServerBaseSync[Req, Resp]:

    import BeastConverters._

    def handlerSync(req: BeastRequestPtr): BeastResponsePtr =

      println("handlerSync 0")

      val request = toRequest(req).asInstanceOf[Req]
      println("handlerSync 1")
      val resp = handle(request)
      println("handlerSync 2")
      Zone:
        implicit z =>
          val r = toResponsePtr(resp)
          println("handlerSync 3")
          r

    override def run: Int =
      Zone:
        implicit z =>
          runBeastSync(
            unsafe.toCString(host),
            port.toUShort,
            workers.toUShort,
            CFuncPtr1.fromScalaFunction(handlerSync))


object Main:
  import beast._
  import beast_server._
  def handlerSync(req: BeastRequestPtr): BeastResponsePtr =
    println(s"handlerSync")
    val resp = unsafe.stackalloc[BeastResponse]()
    val body = unsafe.stackalloc[BeastBody]()
    body._1 = c"OK"
    body._3 = 2
    resp._1 = 200
    resp._2 = c"text/plain"
    resp._3 = body
    resp

  def runCSyncServer(host: String, port: Int, threads: Int) =
    Zone:
      implicit z =>
        runBeastSync(
          unsafe.toCString(host),
          port.toUShort,
          threads.toUShort,
          handlerSync)
  def runAsyncServer(host: String, port: Int, threads: Int) =
    val server = new HttpServerAsync[Request, Response](host, port, threads):
      override def handle(req: Request, cb: Response => Unit): Unit =
        cb(Response(
          statusCode = 200,
          body = Some("[{\"name\": \"ricardo\"}, {\"name\": \"jonas\"}]"),
          contentType = "application/json",
          headers = Map("Token" -> "123")
        ))
    server.run

  def runSyncServer(host: String, port: Int, threads: Int) =
    val server = new HttpServerSync[Request, Response](host, port, threads):
      override def handle(req: Request): Response =
        println("handle")
        Response(
          statusCode = 200,
          body = Some("[{\"name\": \"ricardo\"}, {\"name\": \"jonas\"}]"),
          contentType = "application/json",
          headers = Map("Token" -> "123")
        )

    server.run

  def main(args: Array[String]): Unit =
    val port: Int = 8181
    val threads: Int = 4
    val host = "0.0.0.0"
    println("start start server")

    runCSyncServer(host, port, threads)
    ()

