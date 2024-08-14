package app

import app.Models.Person
import br.com.mobilemind.beast.*
import br.com.mobilemind.beast.HttpStatus.BadRequest
import br.com.mobilemind.json.codec.converter.auto.JsonConverter
import br.com.mobilemind.micro.routing.Path.root
import br.com.mobilemind.micro.routing.router.{Method, RequestBuilder, RouteInfo, Router}
import br.com.mobilemind.micro.routing.router.Router.{after, before, route}

import scala.util.{Failure, Success, Try}

object AppRouter:

  import RequestEntity.toRequestEntity

  given RequestBuilder[HttpRequest, ReqExtra] with
    override def build(
                        routeInfo: RouteInfo,
                        extra: Option[ReqExtra]
                      ): HttpRequest =

      val headers = extra.map(_.headers).getOrElse(Map())
      val contentType = headers
        .find(_._1.toLowerCase == "content-type")
        .map(_._2)
        .map(ContentType.make)

      Request(
        routeInfo.target,
        routeInfo.method.toHttpMethod,
        contentType,
        extra.flatMap(_.body),
        extra.flatMap(_.bodyRaw),
        headers,
        routeInfo.params,
        routeInfo.query,
        routeInfo.matcher
      )


  val indexGet = route[HttpRequest, HttpResponse](Method.Get, root) {
    case (req: Request) =>
      println("indexGet")
      Response(
        status = HttpStatus.OK,
        body = Some("hello!"),
        contentType = ContentType.Text,
      ).asInstanceOf[HttpResponse]
  }

  val indexPost = route[HttpRequest, HttpResponse](Method.Post, root) {
    case (req: Request) =>
      Response(
        status = HttpStatus.OK,
        body = req.body,
        contentType = ContentType.Text,
      ).asInstanceOf[HttpResponse]
  }

  val jsonDecoder = before[HttpRequest, HttpResponse] {
    case (req: Request) =>
      req.body.map { s =>

        Try:
          JsonConverter[Person].fromJson(s)
        match
          case Success(v) =>
            req.toRequestEntity(Some(v))
          case Failure(e) =>
            Response(BadRequest, body = Some(e.getMessage))

      }.getOrElse(req)
  }


  val jsonEncoder = after[HttpRequest, HttpResponse] {
    (_, resp: HttpResponse) =>
      resp match
        case r: ResponseEntity[Person] =>
          r.copy(body = r.entity.map(_.toJson))
        case _ => resp
  }

  val person = route[HttpRequest, HttpResponse](Method.Post, root / "json") {
    case (req: RequestEntity[Person]) =>
      //println(s"${req.entity}")
      ResponseEntity(
        status = HttpStatus.OK,
        contentType = ContentType.Text,
        entity = req.entity
      )

  }

  val personJson = jsonDecoder ++ person ++ jsonEncoder

  def mkRouter: Router[HttpRequest, HttpResponse, ReqExtra] =
    Router(indexGet, indexPost, personJson)


