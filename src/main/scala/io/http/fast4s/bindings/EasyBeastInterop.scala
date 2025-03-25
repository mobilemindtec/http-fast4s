package io.http.fast4s.bindings

import io.http.fast4s.bindings.EasyBeastInterop.{newBody, newHeaders, newResponse}
import io.http.fast4s.bindings.conv.{c_str, str}

import scala.collection.{immutable, mutable}
import scala.language.experimental.namedTuples
import scala.scalanative.unsafe.*
import scala.scalanative.unsafe.linkCppRuntime

object structs:

  // name, value
  type BeastHeader = CStruct2[CString, CString]
  object BeastHeader:
    given _tag: Tag[BeastHeader] = Tag.materializeCStruct2Tag[CString, CString]

    def apply()(using Zone): Ptr[BeastHeader] = alloc[BeastHeader](1)

    def apply(name: CString, value: CString)(using Zone): Ptr[BeastHeader] =
      val ___ptr = apply()
      (!___ptr).name = name
      (!___ptr).value = value
      ___ptr

  extension (struct: BeastHeader)
    def name: CString = struct._1
    def name_=(value: CString): Unit = !struct.at1 = value
    def value: CString = struct._2
    def value_=(value: CString): Unit = !struct.at2 = value

  // header start pointer, size
  type BeastHeaders = CStruct2[Ptr[BeastHeader], CInt]
  object BeastHeaders:
    given _tag: Tag[BeastHeaders] = Tag.materializeCStruct2Tag[Ptr[BeastHeader], CInt]

    def apply()(using Zone): Ptr[BeastHeaders] = alloc[BeastHeaders](1)

    def apply(header: Ptr[BeastHeader], size: CInt)(using Zone): Ptr[BeastHeaders] =
      val ___ptr = apply()
      (!___ptr).header = header
      (!___ptr).size = size
      ___ptr

    def apply(headers: Map[String, String])(using Zone): Ptr[BeastHeaders] =
      if headers.isEmpty
      then null
      else
        val ___ptr = newHeaders(headers.size)
        var pt = ___ptr._1
        for h <- headers do
          (!pt).name = h._1.c_str
          (!pt).value = h._2.c_str
          pt += 1
        ___ptr

  extension (struct: BeastHeaders)
    def header: Ptr[BeastHeader] = struct._1
    def header_=(value: Ptr[BeastHeader]): Unit = !struct.at1 = value
    def size: CInt = struct._2
    def size_=(value: CInt): Unit = !struct.at2 = value

    def headers: Map[String, String] =
      var ptr = struct.header
      val size = struct.size
      val data = mutable.HashMap.empty[String, String]
      for _ <- 0 until size do
        val name = ptr._1.str
        val value = ptr._2.str
        data.addOne(name -> value)
        ptr += 1
      data.toMap

  // body {str, body raw, int}
  type BeastBody = CStruct3[CString, Ptr[Byte], CInt]
  object BeastBody:
    given _tag: Tag[BeastBody] = Tag.materializeCStruct3Tag[CString, CString, CInt]

    def apply()(using Zone): Ptr[BeastBody] = alloc[BeastBody](1)

    def apply(body: CString, buff: CString, size: CInt)(using Zone): Ptr[BeastBody] =
      val ___ptr = apply()
      (!___ptr)._body = body
      (!___ptr)._buff = buff
      (!___ptr)._size = size
      ___ptr

    def apply(body: Option[String], rawBody: Option[immutable.Seq[Byte]])(using Zone): Ptr[BeastBody] =
      val size = rawBody.map(_.length).getOrElse(0)
      val rawBodyPtr =
        if size == 0
        then  null
        else
          val buff = rawBody.get
          val raw = alloc[Byte](size)
          for i <- 0 until size do
            raw(i) = buff(i)
          raw
      newBody(body.map(_.c_str).orNull, rawBodyPtr, size)

  extension (struct: BeastBody)
    def _body: CString = struct._1
    def _body_=(value: CString): Unit = !struct.at1 = value
    def _buff: CString = struct._2
    def _buff_=(value: CString): Unit = !struct.at2 = value
    def _size: CInt = struct._3
    def _size_=(value: CInt): Unit = !struct.at3 = value

    def body: Option[String] =
      if struct._body == null
      then None
      else
        val b = struct._body
        if b != null
        then Some(b.str)
        else None

    def rawBody: Option[immutable.Seq[Byte]] =
      if struct._buff == null
      then None
      else
        val ptr = struct._buff
        val len = struct._size
        val buff = mutable.ArrayBuilder.ofByte()
        for i <- 0 until len do
          buff.addOne(ptr(i))
        Some(buff.result().toSeq)


  // verb, target, content type, {body str, body bytes, size} , {[{name, value], size}
  type BeastRequest = CStruct5[CString, CString, CString, Ptr[BeastBody], Ptr[BeastHeaders]]
  object BeastRequest:
    given _tag: Tag[BeastRequest] = Tag.materializeCStruct5Tag[CString, CString, CString, Ptr[BeastBody], Ptr[BeastHeaders]]

    def apply()(using Zone): Ptr[BeastRequest] = alloc[BeastRequest](1)

    def apply(method: CString, target: CString, contentType: CString, body: Ptr[BeastBody], headers: Ptr[BeastHeaders])(using Zone): Ptr[BeastRequest] =
      val ___ptr = apply()
      (!___ptr)._method = method
      (!___ptr)._target = target
      (!___ptr)._contentType = contentType
      (!___ptr)._body = body
      (!___ptr)._headers = headers
      ___ptr

  extension (struct: BeastRequest)
    def _method: CString = struct._1
    def _method_=(value: CString): Unit = !struct.at1 = value
    def _target: CString = struct._2
    def _target_=(value: CString): Unit = !struct.at2 = value
    def _contentType: CString = struct._3
    def _contentType_=(value: CString): Unit = !struct.at3 = value
    def _body: Ptr[BeastBody] = struct._4
    def _body_=(value: Ptr[BeastBody]): Unit = !struct.at4 = value
    def _headers: Ptr[BeastHeaders] = struct._5
    def _headers_=(value: Ptr[BeastHeaders]): Unit = !struct.at5 = value
    def headers: Map[String, String] = (!struct._headers).headers
    def method: String = struct._method.str
    def target: String = struct._target.str

    def contentType: Option[String] =
      if struct._contentType != null
      then Some(struct._contentType.str)
      else None

    def body: Option[String] =
      if struct._body == null
      then None
      else
        (!struct._body).body

    def rawBody: Option[immutable.Seq[Byte]] =
      if struct._body == null
      then None
      else
        (!struct._body).rawBody


  // status {code, content type, {body str, body bytes, size}, {[{name, value], size}}
  type BeastResponse = CStruct4[CInt, CString, Ptr[BeastBody], Ptr[BeastHeaders]]
  object BeastResponse:
    given _tag: Tag[BeastResponse] = Tag.materializeCStruct4Tag[CInt, CString, Ptr[BeastBody], Ptr[BeastHeaders]]

    def apply()(using Zone): Ptr[BeastResponse] = alloc[BeastResponse](1)

    def apply(status: CInt, contentType: CString, body: Ptr[BeastBody], headers: Ptr[BeastHeaders])(using Zone): Ptr[BeastResponse] =
      val ___ptr = apply()
      (!___ptr).status = status
      (!___ptr)._contentType = contentType
      (!___ptr)._body = body
      (!___ptr)._headers = headers
      ___ptr

    def apply(status: Int, contentType: String, body: Option[String], rawBody: Option[immutable.Seq[Byte]], headers: Map[String, String])(using Zone): Ptr[BeastResponse] =
      val resp = newResponse(status)
      (!resp)._contentType =  contentType.c_str
      (!resp)._body = BeastBody(body, rawBody)
      (!resp)._headers = BeastHeaders(headers)
      resp

  extension (struct: BeastResponse)
    def status: CInt = struct._1
    def status_=(value: CInt): Unit = !struct.at1 = value
    def _contentType: CString = struct._2
    def _contentType_=(value: CString): Unit = !struct.at2 = value
    def _body: Ptr[BeastBody] = struct._3
    def _body_=(value: Ptr[BeastBody]): Unit = !struct.at3 = value
    def _headers: Ptr[BeastHeaders] = struct._4
    def _headers_=(value: Ptr[BeastHeaders]): Unit = !struct.at4 = value

    def headers: Map[String, String] = (!struct._headers).headers

    def contentType: Option[String] =
      if struct._contentType != null
      then Some(struct._contentType.str)
      else None

    def body: Option[String] =
      if struct._body == null
      then None
      else (!struct._body).body

    def rawBody: Option[immutable.Seq[Byte]] =
      if struct._body == null
      then None
      else (!struct._body).rawBody


  type BeastHandlerCallback = CFuncPtr2[Ptr[BeastRequest], Ptr[BeastResponse], Unit]
  type BeastHttpHandlerSync = CFuncPtr1[Ptr[BeastRequest], Ptr[BeastResponse]]
  type BeastHttpHandlerAsync = CFuncPtr2[Ptr[BeastRequest], BeastHandlerCallback, Unit]

  type ThreadInit = CFuncPtr1[Ptr[Byte], Unit]
  type ThreadStarter = CFuncPtr3[ThreadInit, CInt, Ptr[Byte], Unit]

@linkCppRuntime
@link("EasyBeast")
@extern
object EasyBeastInterop:

  import structs.*

  @name("run_sync")
  def runBeastSync(hostname: CString,
                   port: CUnsignedShort,
                   maxThread: CUnsignedShort,
                   threadStarter: ThreadStarter,
                   handler: BeastHttpHandlerSync): CInt = extern

  @name("run_async")
  def runBeastAsync(hostname: CString,
                    port: CUnsignedShort,
                    maxThread: CUnsignedShort,
                    threadStarter: ThreadStarter,
                    handler: CFuncPtr): CInt = extern


  @name("response_new")
  def newResponse(statusCode: CInt): Ptr[BeastResponse] = extern

  @name("body_new")
  def newBody(body: CString, rawBody: CString, size: CInt): Ptr[BeastBody] = extern

  @name("headers_new")
  def newHeaders(size: CInt): Ptr[BeastHeaders] = extern
