package br.com.mobilemind.beast

trait HttpResponse:

  val status: HttpStatus
  val contentType: ContentType
  val body: Option[String] = None
  val bodyRaw: Option[Seq[Byte]] = None
  val headers: Headers = Map()

  def hasBody: Boolean = hasBodyStr || hasBodyRaw

  def hasBodyStr: Boolean = body.nonEmpty

  def hasBodyRaw: Boolean = bodyRaw.nonEmpty

  def bodySize: Int =
    body.map(_.length).getOrElse(bodyRaw.map(_.size).getOrElse(0))