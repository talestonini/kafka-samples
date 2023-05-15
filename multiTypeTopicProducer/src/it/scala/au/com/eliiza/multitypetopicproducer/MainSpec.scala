package au.com.eliiza.multitypetopicproducer

import au.com.eliiza.common._
import au.com.eliiza.model._
import scala.concurrent.duration._
import scala.language.postfixOps

class MainSpec extends BaseItSpec {

  val consumer = new Consumer(conf, processFn)

  def createTopics(admin: Admin): Unit =
    admin.createTopics("user-event")

  def registerSchemas(admin: Admin): Unit = {
    admin.registerSchema("user-create-event-value", "avro/UserCreateEvent.avsc")
    admin.registerSchema("user-update-event-value", "avro/UserUpdateEvent.avsc")
    admin.registerSchema("user-event-value", "avro/UserEvent.avsc_", Some("avro/UserEventRefs.json"))
  }

  override protected def afterAll(): Unit = {
    consumer.close()
  }

  "This app" should "be able to produce records of different types into the same Kafka topic" in {
    Main.main(conf)
    val data = consumer.consumeInSinglePoll(conf.default.getString("kafka.topic"), 15 seconds)

    // just to prove we can find which type of event it is when topic contains multiple types
    data.foreach {
      case uc: UserCreateEvent => logger.info(">>> consumed a user-create-event")
      case uu: UserUpdateEvent => logger.info(">>> consumed a user-update-event")
    }

    data.contains(UserCreateEvent("jr", "John Rambo", 33)) shouldBe true
    data.contains(UserUpdateEvent("Johnny Rambo", 30)) shouldBe true
  }

  private def processFn[V](key: String, value: V): Either[Exception, Any] =
    Right(logger.info(s">>> consumed: key='$key' value=$value"))

}
