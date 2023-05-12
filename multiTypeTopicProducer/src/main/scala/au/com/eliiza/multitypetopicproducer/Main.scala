package au.com.eliiza.multitypetopicproducer

import au.com.eliiza.common.{CommonMain, Config, Producer}
import au.com.eliiza.model._

object Main extends CommonMain {

  override def main(conf: Config): Unit = {
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
