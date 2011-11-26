package gt4s

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import util.Properties

object Hello extends cycle.Plan with cycle.ThreadPool with ServerErrorResponse {
  def intent = {
		 case GET(_) => Ok ~> ResponseString("Welcome to gt4s")
  }
}

object Start extends App {
  val port = Properties.envOrElse("PORT", "80").toInt
  println("Starting on port:"+port)
  unfiltered.netty.Http(port).plan(Hello).run()
}
