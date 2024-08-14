package br.com.mobilemind.beast

import br.com.mobilemind.beast.beast_server.*

import scala.collection.mutable
import scala.scalanative.runtime.ffi.malloc
import scala.scalanative.runtime.fromRawPtr
import scala.scalanative.*
import scala.scalanative.unsafe.{CString, Zone, fromCString}
import scala.scalanative.unsigned.UnsignedRichInt

type Headers = Map[String, String]

object converters:

  extension (cs: CString)
    def str = fromCString(cs)

  extension (s: String)(using Zone)
    def c_str =
      unsafe.toCString(s)

  extension(req: BeastRequestPtr)
    def request(): Request =

      var bodyRaw: Option[Seq[Byte]] = None
      var body: Option[String] = None
      val headers = mutable.Map[String, String]()

      if req._4 != null then
        val bodyPtr = req._4
        val bodyStr = bodyPtr._1
        var bodyBytes = bodyPtr._2
        val bodyLen = bodyPtr._3

        if bodyStr != null then
          body = Some(bodyStr.str)

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
          headers(headerPtr._1.str) = headerPtr._2.str
          headerPtr += 1

      val verb = req._1.str
      val uri = req._2.str
      val ctype = req._3.str

      Request(
        method = HttpMethod(verb),
        target = uri,
        contentType = Some(ContentType.make(ctype)),
        body = body,
        bodyRaw = bodyRaw,
        headers = headers.toMap,
      )

  extension(response: HttpResponse)
    // beast struct {status code, content type, {body str, body bytes, size}, {[{name, value], size}}
    def response()(using Zone): BeastResponsePtr =
      val resp = fromRawPtr[BeastResponse](malloc(unsafe.sizeof[BeastResponse]))
      resp._1 = response.status.code
      resp._2 = response.contentType.mimeType.c_str
      resp._3 = null
      resp._4 = null

      if response.hasBody then

        val body = fromRawPtr[BeastBody](malloc(unsafe.sizeof[BeastBody]))
        body._1 = null
        body._2 = null
        body._3 = response.bodySize

        if response.hasBodyStr then
          val str = response.body.get
          body._1 = str.c_str
          body._3 = str.length
        else
          val bsize = response.bodySize
          var bytes = fromRawPtr[Byte](malloc(unsafe.sizeof[Byte] * bsize.toUInt))
          val raw = response.bodyRaw.get
          for b <- raw do
            bytes `unary_!_=` b
            bytes += 1
          body._2 = bytes
          body._3 = raw.length
        resp._3 = body

      if response.headers.nonEmpty then
        val hsize = response.headers.size
        val headers = fromRawPtr[BeastHeaders](malloc(unsafe.sizeof[BeastHeaders]))
        headers._1 = fromRawPtr[BeastHeader](malloc(unsafe.sizeof[BeastHeader] * hsize.toUInt))

        headers._2 = hsize

        for h <- response.headers; i <- 0 until hsize do
          val header = fromRawPtr[BeastHeader](malloc(unsafe.sizeof[BeastHeader]))
          header._1 = h._1.c_str
          header._2 = h._2.c_str
          headers._1(i) = header

        resp._4 = headers
      resp