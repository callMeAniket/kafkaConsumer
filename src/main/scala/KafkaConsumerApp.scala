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

    val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(kafkaConfig.getString("bootstrap.servers"))
      .withGroupId("group1")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    Consumer.plainSource(consumerSettings, Subscriptions.topics("tickets"))
      .map(_.value().parseJson.convertTo[Ticket]) // Convert JSON string to Person
      .runWith(Sink.foreach(person => println(s"Received ticket: $person")))
  }
}
