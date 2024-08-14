package app


import app.AppRouter.mkRouter
import br.com.mobilemind.beast.*
import br.com.mobilemind.micro.routing.router.Router



def runAsyncServer(host: String, port: Int, threads: Int) =
  val server = new HttpServerAsync[HttpRequest, HttpResponse](host, port, threads):

    def router: Router[HttpRequest, HttpResponse, ReqExtra] = mkRouter

    override def fail(request: HttpRequest, status: HttpStatus): HttpResponse =
      Response(
        status = HttpStatus.NotFound,
        body = Some("not found"),
        contentType = ContentType.Text
      )

  server.run

@main def main(args: String*): Int =
  val port: Int = 8181
  val threads: Int = 5
  val host = "0.0.0.0"
  runAsyncServer(host, port, threads)
