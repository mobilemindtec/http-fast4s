package io.http.fast4s

import io.http.fast4s.bindings.conv.*
import io.http.fast4s.types.*
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.*

import scala.scalanative.unsafe.Zone

class BeastStructSpec extends AnyFunSuite with Matchers:

  test("struct converter test"){

    Zone:
      val req = Request(
        method = HttpMethod.Get,
        target = "/",
        contentType = None,
        body = Some("request!"),
        headers = Map("Content-Type" -> "application/json; charset=UTF-8", "Accept-Encoding" -> "gzip", "Accept-Language" -> "en-US,es;q=0.5"),
      )

      val reqPtr = req.ptr()
      val reqOther = reqPtr.request()
      reqOther `shouldBe` req

      val resp = Response(
        status = HttpStatus.OK,
        body = Some("response!!"),
        headers = Map("Content-Type" -> "application/json; charset=UTF-8", "Accept-Encoding" -> "gzip", "Accept-Language" -> "en-US,es;q=0.5")
      )

      val respPtr = resp.ptr()
      val respOther = respPtr.response()
      respOther `shouldBe` resp
  }

  test("struct converter raw body") {

    Zone:
      val req = Request(
        method = HttpMethod.Get,
        target = "/",
        rawBody = Some(Seq('A', 'B', 'C', 'D')),
        headers = Map("Content-Type" -> "application/json; charset=UTF-8", "Accept-Encoding" -> "gzip", "Accept-Language" -> "en-US,es;q=0.5"),
      )

      val reqPtr = req.ptr()
      val reqOther = reqPtr.request()

      reqOther `shouldBe` req

      val resp = Response(
        status = HttpStatus.OK,
        rawBody = Some(Seq('A', 'B', 'C', 'D')),
        headers = Map("Content-Type" -> "application/json; charset=UTF-8", "Accept-Encoding" -> "gzip", "Accept-Language" -> "en-US,es;q=0.5")
      )

      val respPtr = resp.ptr()
      val respOther = respPtr.response()

      respOther `shouldBe` resp
  }




