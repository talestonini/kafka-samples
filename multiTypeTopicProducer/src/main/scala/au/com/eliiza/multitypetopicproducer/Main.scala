package au.com.eliiza.multitypetopicproducer

import au.com.eliiza.common.Config
import au.com.eliiza.model._
import org.apache.avro.Schema
import scala.util.{Success, Failure}

object Main {

  def main(args: Array[String]): Unit = {
    val config = for {
      args <- Config.validateArgs(args.toList)
      conf <- Config.getConfig(args)
    } yield conf

    config match {
      case Success(conf) =>
        println("starting app...")
        demoDataProduction(conf)
      case Failure(e) =>
        println(s"unable to start app: ${e.getMessage()}")
    }
  }

  def demoDataProduction(conf: Config): Unit = {
    // producers
    val userCreateEventProducer = new Producer[UserCreateEvent](conf)
    sys.addShutdownHook(userCreateEventProducer.close())
    val userUpdateEventProducer = new Producer[UserUpdateEvent](conf)
    sys.addShutdownHook(userUpdateEventProducer.close())

    // data production
    val topic = conf.default.getString("kafka.topic")
    userCreateEventProducer.produce(topic, "jd", UserCreateEvent("jd", "John Doe"))
    userUpdateEventProducer.produce(topic, "jd", UserUpdateEvent("jd", "Johnny Doe"))
  }

}
