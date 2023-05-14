package au.com.eliiza.common

import au.com.eliiza.model._
import scala.concurrent.duration._
import scala.language.postfixOps

class ConsumerSpec extends BaseItSpec {

  val admin                   = new Admin(conf)
  val userCreateEventProducer = new Producer[UserCreateEvent](conf)
  val consumer                = new Consumer(conf, processFn)
  val topic                   = conf.default.getString("kafka.topic")

  override protected def beforeAll(): Unit = {
    // create topics
    admin.createTopics("user-event")

    // register schemas
    admin.registerSchema("user-create-event-value", "avro/UserCreateEvent.avsc")
    admin.registerSchema("user-update-event-value", "avro/UserUpdateEvent.avsc")
    admin.registerSchema("user-event-value", "avro/UserEvent.avsc_", Some("avro/UserEventRefs.json"))
  }

  override protected def afterAll(): Unit = {
    admin.close()
    userCreateEventProducer.close()
    consumer.close()
  }

  "A Consumer" should "be able to consume records from a Kafka topic" in {
    val event = UserCreateEvent("jd", "John Doe")
    userCreateEventProducer.produce(topic, "jd", event)
    val data = consumer.consumeOnce(topic, 15 seconds)
    data.contains(event) shouldBe true
  }

  private def processFn[V](key: String, value: V): Either[Exception, Any] =
    Right(logger.info(s">>> consumed: key='$key' value=$value"))

}
