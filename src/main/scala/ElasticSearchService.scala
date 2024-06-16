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
  def pushToElastic(ticketFormat: Ticket): Unit = {
    val ticket: Ticket12 = Ticket12(ticketFormat.id, ticketFormat.title, ticketFormat.description, ticketFormat.department, ticketFormat.status, ticketFormat.assignedTo)
    implicit val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "SingleRequest")
    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    val jsonRequest = ticket.toJson.compactPrint
    val entity = HttpEntity(ContentTypes.`application/json`, jsonRequest)
    val url = "http://localhost:9200/my_index/_doc"

    val request = HttpRequest(method = POST, uri = url, entity = entity)

    val responseFuture: Future[HttpResponse] = Http().singleRequest(request.withHeaders(Authorization(BasicHttpCredentials("elastic", "changeme"))))
    responseFuture
      .onComplete {
        case Success(res) => println(res)
        case Failure(_) => sys.error("something wrong")
      }
  }
}