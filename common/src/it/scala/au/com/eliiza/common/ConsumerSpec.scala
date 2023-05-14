package au.com.eliiza.common

import au.com.eliiza.model._
import scala.concurrent.duration._
import scala.language.postfixOps

class ConsumerSpec extends BaseItSpec {

  val admin    = new Admin(conf)
  val producer = new Producer[UserCreateEvent](conf)
  val consumer = new Consumer(conf, processFn)
  val topic    = conf.default.getString("kafka.user-create-topic")

  override protected def beforeAll(): Unit = {
    // create topics
    admin.createTopics("user-create-event")

    // register schemas
    admin.registerSchema("user-create-event-value", "avro/UserCreateEvent.avsc")
  }

  override protected def afterAll(): Unit = {
    admin.close()
    producer.close()
    consumer.close()
  }

  "A Consumer" should "be able to consume records from a Kafka topic" in {
    val johnEvent = UserCreateEvent("jd", "John Doe", 1)
    val maryEvent = UserCreateEvent("ms", "Mary Smith", 2)

    producer.produce(topic, "jd", johnEvent)
    producer.produce(topic, "ms", maryEvent)

    val data = consumer.consumeOnce(topic, 15 seconds)

    data.contains(johnEvent) shouldBe true
    data.contains(maryEvent) shouldBe true
  }

  private def processFn[V](key: String, value: V): Either[Exception, Any] =
    Right(logger.info(s">>> consumed: key='$key' value=$value"))

}
