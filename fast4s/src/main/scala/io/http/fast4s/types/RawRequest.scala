package io.http.fast4s.types

import io.http.fast4s.beast.Headers

case class RawRequest(body: String = "",
                      bodyRaw: Seq[Byte] = Nil,
                      headers: Headers = Map())