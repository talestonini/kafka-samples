package au.com.eliiza.multitypetopicproducer

import au.com.eliiza.common.{CommonMain, Config, Producer}
import au.com.eliiza.model._

object Main extends CommonMain {

  override def main(conf: Config): Unit = {
    // producers
    val userCreateEventProducer = new Producer[UserCreateEvent](conf)
    val userUpdateEventProducer = new Producer[UserUpdateEvent](conf)

    // data production
    val topic = conf.default.getString("kafka.topic")
    userCreateEventProducer.produce(topic, "jr", UserCreateEvent("jr", "John Rambo", 33))
    userUpdateEventProducer.produce(topic, "jr", UserUpdateEvent("Johnny Rambo", 30))

    userCreateEventProducer.close()
    userUpdateEventProducer.close()
  }

}
