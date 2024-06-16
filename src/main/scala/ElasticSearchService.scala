import TicketJsonProtocol.ticketFormat
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import spray.json._

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

case class Ticket12(id: Int, title: String, description: String, department: String, status: String, assignedTo: Int)

object TicketJsonProtocol extends DefaultJsonProtocol {
  implicit val ticketFormat: RootJsonFormat[Ticket12] = jsonFormat6(Ticket12)
}

class ElasticSearchService {
  def pushToElastic(ticket: Ticket12): Unit = {
    // Ensure that the implicit ticketFormat is in scope
    import TicketJsonProtocol._

    implicit val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "SingleRequest")
    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    val jsonRequest = ticket.toJson.compactPrint
    val entity = HttpEntity(ContentTypes.`application/json`, jsonRequest)
    val url = "http://34.170.165.7/:9200/my_index/_doc"

    val request = HttpRequest(method = HttpMethods.POST, uri = url, entity = entity)
      .addCredentials(BasicHttpCredentials("elastic", "changeme"))

    val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
    responseFuture
      .onComplete {
        case Success(res) => println(res)
        case Failure(h) => sys.error(s" Aniket_log something went wrong $h" )
      }
  }
}
