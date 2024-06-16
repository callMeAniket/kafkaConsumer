import spray.json._

case class Ticket(
                   id: Int,
                   title: String,
                   description: String,
                   department: String,
                   status: String,
                   assignedTo: Int
                 )

object JsonFormats {

  import DefaultJsonProtocol._

  implicit val personFormat: RootJsonFormat[Ticket] = jsonFormat6(Ticket)
}