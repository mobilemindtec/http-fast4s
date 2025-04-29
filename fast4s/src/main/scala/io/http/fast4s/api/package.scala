package io.http.fast4s

package object api:

  export io.http.fast4s.app.Fast4s.fast
  export io.http.fast4s.app.extra.given_ReqBuilder

  export io.http.fast4s.core.{HttpServerBuilder, ReqBuilder}
  export io.http.fast4s.types.{
    ContentType,
    HttpMethod,
    HttpStatus,
    MimeType,
    Request,
    Response
  }
  export io.http.fast4s.types.HttpMethod.{
    Head,
    Options,
    Patch,
    Get,
    Post,
    Put,
    Delete,
    Trace,
    Connect,
  }
  export io.http.fast4s.core.HttpServerBuilder.newHttpServerBuilder

  // router
  //export io.micro.router.*
