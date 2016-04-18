package rip.hansolo

import org.http4s._
import org.http4s.dsl._
import org.http4s.server.blaze.BlazeBuilder

/**
  * Created by giymo11 on 18.04.16.
  */
object YangenServer extends App {

  val yangenService = HttpService {
    case GET -> Root =>
      Ok("Hello World")
  }

  BlazeBuilder
    .bindHttp(8081, "0.0.0.0")
    .mountService(yangenService)
    .run
    .awaitShutdown()
}
