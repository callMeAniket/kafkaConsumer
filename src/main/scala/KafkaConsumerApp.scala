import JsonFormats._
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import spray.json._

object KafkaConsumerApp {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem[_] = ActorSystem(Behaviors.empty, "kafkaConsumerSystem")

    val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers("kafka1:9092,kafka2:9093,kafka3:9094")
      .withGroupId("group1")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    Consumer.plainSource(consumerSettings, Subscriptions.topics("someTopic"))
      .map(_.value().parseJson.convertTo[Person]) // Convert JSON string to Person
      .runWith(Sink.foreach(person => println(s"Received person: $person")))
  }
}