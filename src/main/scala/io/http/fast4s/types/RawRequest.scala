package io.http.fast4s.types

import io.http.fast4s.bindings.Headers
import scala.collection.immutable

case class RawRequest(body: String = "",
                      bodyRaw: Seq[Byte] = Nil,
                      headers: Headers = Map())