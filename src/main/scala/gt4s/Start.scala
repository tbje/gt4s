package gt4s

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import util.Properties

object Hello extends cycle.Plan with cycle.ThreadPool with ServerErrorResponse {
  def classPathInputStream(path :String) = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)

  def index = 
    Option(classPathInputStream("index.html")).map { is => io.Source.fromInputStream(is).map(_.toByte).toArray }
  
  def intent = {
    case POST(Path("/enc") & Params(params)) =>
      params("q") match {
        case Seq(q) => Ok ~> ResponseString(scala.reflect.NameTransformer.encode(q))
        case _ =>       BadRequest ~> ResponseString("Post data to be encoded in q parameter.")
      }
    case POST(Path("/dec") & Params(params)) =>
      params("q") match {
        case Seq(q) => Ok ~> ResponseString(scala.reflect.NameTransformer.decode(q))
        case _ =>       BadRequest ~> ResponseString("Post data to be encoded in q parameter.")
      }
    case GET(Path("/enc") | Path("/dec")) =>
      MethodNotAllowed ~> ResponseString("Use POST HTTP method.")
    case GET(_) =>  index.map { 
                      p => Ok ~>  ResponseBytes(p) 
                    }.getOrElse{ 
                      InternalServerError ~> ResponseString("I am sorry I cannot load your request :( I expected to be able to load a file but, alas, I could not.") 
                    }
  }
}

object Start extends App {
  val port = Properties.envOrElse("PORT", "8080").toInt
  println("Starting on port:"+port)
  unfiltered.netty.Http(port).plan(Hello).run()
}
