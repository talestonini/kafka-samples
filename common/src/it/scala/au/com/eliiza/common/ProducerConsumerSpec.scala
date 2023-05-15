package au.com.eliiza.common

import au.com.eliiza.model._
import scala.concurrent.duration._
import scala.language.postfixOps

class ProducerConsumerSpec extends BaseItSpec {

  val producer                 = new Producer[UserCreateEvent](conf)
  val consumer                 = new Consumer(conf, processFn)
  val consumerWithBadProcessFn = new Consumer(conf, badProcessFn)

  val topic     = conf.default.getString("kafka.user-create-topic")
  val johnEvent = UserCreateEvent("jd", "John Doe", 1)
  val maryEvent = UserCreateEvent("ms", "Mary Smith", 2)

  def createTopics(admin: Admin): Unit =
    admin.createTopics(topic)

  def registerSchemas(admin: Admin): Unit =
    admin.registerSchema("user-create-event-value", "avro/UserCreateEvent.avsc")

  override protected def afterAll(): Unit = {
    producer.close()
    consumer.close()
    consumerWithBadProcessFn.close()
  }

  "A Producer and a Consumer" should "be able to produce and consume records into and from a topic" in {
    producer.produce(topic, "jd", johnEvent)
    producer.produce(topic, "ms", maryEvent)
    val data = consumer.consumeInSinglePoll(topic, 15 seconds)

    data.contains(johnEvent) shouldBe true
    data.contains(maryEvent) shouldBe true
  }

  "A Consumer" should "interrupt when unable to process a record" in {
    producer.produce(topic, "jd", johnEvent)
    producer.produce(topic, "ms", maryEvent)
    val data = consumerWithBadProcessFn.consumeInSinglePoll(topic, 15 seconds)

    data.length shouldBe 1
  }

  private def processFn[V](key: String, value: V): Either[Exception, Any] =
    Right(logger.info(s">>> consumed: key='$key' value=$value"))

  private def badProcessFn[V](key: String, value: V): Either[Exception, Any] =
    Left(new Exception(s"unable to process record: key='$key' value=$value"))

}
