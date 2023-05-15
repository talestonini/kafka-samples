package au.com.eliiza.common

import io.confluent.kafka.serializers.{AbstractKafkaSchemaSerDeConfig, KafkaAvroDeserializer}
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord, ConsumerRecords, KafkaConsumer}
import org.apache.kafka.common.errors.WakeupException
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.{Logger, LoggerFactory}
import java.{util => ju}
import scala.collection.mutable.ListBuffer
import scala.concurrent.Promise
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._
import scala.jdk.javaapi.DurationConverters
import scala.language.postfixOps

class Consumer[V](conf: Config, processFn: (String, V) => Either[Exception, Any]) {

  private lazy val logger: Logger = LoggerFactory.getLogger(this.getClass())

  logger.debug(s">>> building kafka consumer")
  private val kc = new KafkaConsumer[String, V](kafkaProps().asJava)

  // this is intended to be used in testing
  def consumeInSinglePoll(topic: String, pollDuration: FiniteDuration): List[V] =
    consume(topic, false, false, pollDuration)

  // this is intended to be used in production
  def consume(topic: String, handleInterruption: Boolean = true, pollForever: Boolean = true,
    pollDuration: FiniteDuration = 100 millis): List[V] = {

    // handle shutdown, ie interrupting the consumer
    if (handleInterruption) {
      val mainThread = Thread.currentThread()
      Runtime
        .getRuntime()
        .addShutdownHook(new Thread() {
          override def run(): Unit = {
            logger.info(">>> detected shutdown, waking up the consumer...")
            kc.wakeup()
            try {
              mainThread.join()
            } catch {
              case ie: InterruptedException => ie.printStackTrace()
            }
          }
        })
    }

    var data = ListBuffer[V]()
    try {
      kc.subscribe(List(topic).asJava)
      var abort = false

      while (!abort) {
        val records: ConsumerRecords[String, V]        = kc.poll(DurationConverters.toJava(pollDuration))
        val it: ju.Iterator[ConsumerRecord[String, V]] = records.iterator()
        while (!abort && it.hasNext()) {
          val r = it.next()
          data += r.value()

          // process the consumed record
          processFn(r.key, r.value) match {
            case Left(e) =>
              logger.error(s">>> processing record with key '${r.key}' failed")
              abort = true
            case _ =>
              kc.commitSync()
          }
        }
        abort = !pollForever
      }
    } catch {
      case we: WakeupException => // we ignore this as this is an expected exception when closing a consumer
      case t: Throwable        => logger.error(s">>> unexpected exception closing the consumer: ${t.getMessage()}")
    } finally {
      close()
    }

    data.toList
  }

  def close(): Unit = {
    logger.info(s">>> closing kafka consumer")
    kc.close()
  }

  private def kafkaProps(): Map[String, Any] = {
    val map = Map(
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG                   -> conf.default.getString("kafka.bootstrap-servers"),
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG              -> classOf[StringDeserializer],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG            -> classOf[KafkaAvroDeserializer],
      ConsumerConfig.GROUP_ID_CONFIG                            -> conf.default.getString("kafka.consumer-group"),
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG                   -> "earliest",
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG                  -> false,
      ConsumerConfig.MAX_POLL_RECORDS_CONFIG                    -> 100,
      AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> conf.default.getString("kafka.schema-registry-url"),
      AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS      -> false,
      AbstractKafkaSchemaSerDeConfig.USE_LATEST_VERSION         -> true,
      "specific.avro.reader"                                    -> true
    ) ++ conf.extraAndOverride
    logger.debug(s">>> all consumer kafka props: $map")
    map
  }

}
