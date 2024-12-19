package io.http.fast4s.types

import io.http.fast4s.bindings.Headers
import scala.collection.immutable

case class RawRequest(body: Option[String] = None,
                      bodyRaw: Option[immutable.Seq[Byte]] = None,
                      headers: Headers = Map())