package au.com.eliiza.multitypetopicproducer

import io.confluent.kafka.serializers.{AbstractKafkaAvroSerDeConfig, KafkaAvroSerializer}
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerConfig, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.{Logger, LoggerFactory}
import scala.concurrent.Promise
import scala.io.Source
import scala.jdk.CollectionConverters._

class Producer[V](conf: Config) {

  private lazy val logger: Logger = LoggerFactory.getLogger(this.getClass())

  logger.debug(s"building kafka producer")
  private val kp = new KafkaProducer[String, V](kafkaProps().asJava)

  def produce(topic: String, key: String, value: V): Promise[Either[Exception, RecordMetadata]] = {
    val record  = new ProducerRecord[String, V](topic, key, value)
    val promise = Promise[Either[Exception, RecordMetadata]]()
    kp.send(record,
      new Callback() {
        override def onCompletion(md: RecordMetadata, e: Exception): Unit = {
          if (Option(e).isEmpty) {
            logger.debug(s"produced into topic-partition:offset: $topic-${md.partition()}:${md.offset()}")
            promise.success(Right(md))
          } else {
            logger.error(s"failed producing into topic $topic: ${e.getMessage()}")
            promise.failure(e)
          }
        }
      })
    promise
  }

  def close(): Unit = {
    logger.debug(s"closing kafka producer")
    kp.close()
  }

  private def kafkaProps(): Map[String, Any] = {
    val map = Map(
      ProducerConfig.BOOTSTRAP_SERVERS_CONFIG                 -> conf.kafkaBootstrapServers,
      ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG              -> classOf[StringSerializer],
      ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG            -> classOf[KafkaAvroSerializer],
      AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> conf.schemaRegistryUrl,
      "auto.register.schemas"                                 -> false,
      "use.latest.version"                                    -> true
    ) ++ Producer.loadProps(conf.extraKafkaPropsFromFile)
    logger.debug(s"all producer kafka props: $map")
    map
  }

}

object Producer {

  def loadProps(fromFile: String): Map[String, Any] =
    if (fromFile.isEmpty())
      Map.empty
    else
      (for {
        line <- Source.fromFile(fromFile).getLines()
        if !line.trim().isEmpty() && !line.trim().startsWith("#")
        entry = line.split("=", 2)
      } yield (entry(0) -> entry(1))).toMap

}
