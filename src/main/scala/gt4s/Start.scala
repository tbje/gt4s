package gt4s

import unfiltered.request._
import unfiltered.response.{ Stream => _, _ }
import unfiltered.netty._
import util.Properties
import java.io.{ InputStream, OutputStream }

object Hello extends cycle.Plan with cycle.ThreadPool with ServerErrorResponse {
	abstract sealed class StaticFile(val path: String)
	case object Index extends StaticFile("index.html") 		

	def using[T <: { def close() }] (resource: T)(block: T => Unit) {
		try { block(resource) } finally { if (resource != null) resource.close() }
  }

	object File {
		private def classPathInputStream(path: String) = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)		
		def apply (file: StaticFile) = Option(classPathInputStream(file.path)) match {
				case Some(is) => 
					Ok ~> FileStreamer(is)
				case None =>
					val msg = "I am sorry I cannot load your request :( I expected to be able to load a file but, alas, I could not."	
					InternalServerError ~> ResponseString(msg)
			}
	}

	case class FileStreamer (is: InputStream) extends ResponseStreamer {
		def stream(os: OutputStream) = using(is)(is=>Stream.continually(is.read).takeWhile(-1 !=).foreach(os.write(_)))
	}

  def intent = {
    case POST(Path("/enc") & Params(params)) =>
      params("q") match {
        case Seq(q) =>	Ok ~> ResponseString(scala.reflect.NameTransformer.encode(q))
        case _ =>       BadRequest ~> ResponseString("Post data to be encoded in q parameter.")
      }
    case POST(Path("/dec") & Params(params)) =>
      params("q") match {
        case Seq(q) =>	Ok ~> ResponseString(scala.reflect.NameTransformer.decode(q))
        case _ =>       BadRequest ~> ResponseString("Post data to be encoded in q parameter.")
      }
    case POST(Path("/encRedirect") & Params(params)) =>
      params("q") match {
        case Seq(q) =>	Ok ~> Redirect("http://www.google.com/search?q=%s" format scala.reflect.NameTransformer.encode(q))
        case _ =>       BadRequest ~> ResponseString("Post data to be encoded in q parameter.")
      }
    case GET(Path("/enc") | Path("/dec")) =>
      MethodNotAllowed ~> ResponseString("Use POST HTTP method.")
    case GET(_) =>	
		 	File(Index) 
  }
}

object Start extends App {
  val port = Properties.envOrElse("PORT", "8080").toInt
  println("Starting on port:"+port)
  unfiltered.netty.Http(port).plan(Hello).run()
}
