package au.com.eliiza.common

import au.com.eliiza.model._
import org.scalatest._
import flatspec._
import matchers._
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Success, Failure}

class ConsumerSpec extends AnyFlatSpec with should.Matchers with BeforeAndAfterAll {

  val conf: Config = (for {
    args <- Config.validateArgs(List("test"))
    conf <- Config.getConfig(args)
  } yield conf) match {
    case Success(c) => c
    case Failure(e) => throw new Exception(s"unable to load test config: ${e.getMessage()}")
  }

  val topic                   = conf.default.getString("kafka.topic")
  val userCreateEventProducer = new Producer[UserCreateEvent](conf)
  val consumer                = new Consumer(conf, processFn)

  override protected def afterAll(): Unit = {
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
    Right(println(s"consumed: key='$key' value=$value"))

}
