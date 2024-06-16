import JsonFormats._
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.Sink
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import spray.json._

object KafkaConsumerApp {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem[_] = ActorSystem(Behaviors.empty, "kafkaConsumerSystem")

    val config = ConfigFactory.load()
    val kafkaConfig = config.getConfig("kafka")
    val topic = kafkaConfig.getString("topic")
    val elasticSearchService:ElasticSearchService = new ElasticSearchService
    val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(kafkaConfig.getString("bootstrap.servers"))
      .withGroupId("group1")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    Consumer.plainSource(consumerSettings, Subscriptions.topics(topic))
      .map(_.value().parseJson.convertTo[Ticket]) // Convert JSON string to Person
      .runWith(Sink.foreach(ticket => {
        println(s"Received ticket: $ticket")
        val ticket12:Ticket12 = Ticket12(ticket.id, ticket.title, ticket.description, ticket.department, ticket.status, ticket.assignedTo)
        elasticSearchService.pushToElastic(ticket12)
      }))
  }
}
