package br.com.mobilemind.beast

case class Response(override val status: HttpStatus,
               override val contentType: ContentType = ContentType.Text,
               override val body: Option[String] = None,
               override val bodyRaw: Option[Seq[Byte]] = None,
               override val headers: Headers = Map())
  extends HttpResponse


case class ResponseEntity[T](override val status: HttpStatus,
               override val contentType: ContentType = ContentType.Text,
               override val body: Option[String] = None,
               override val bodyRaw: Option[Seq[Byte]] = None,
               override val headers: Headers = Map(),
                             entity: Option[T] = None)
  extends HttpResponse
