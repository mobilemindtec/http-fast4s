import scalanative.unsafe._


@extern
object lib {
  def add3(i: CInt): CInt = extern
  def cprintln(text: CString): Unit = extern

}

@extern
object httpserver {
  def run(hostname: CString, port: CInt): CInt = extern
}



object Main {
  import lib._
  import httpserver._
  def main(args: Array[String]): Unit =
    val res = add3(3)
    cprintln(c"Hello, world!")

    run(c"0.0.0.0", 8081)
    ()
}
