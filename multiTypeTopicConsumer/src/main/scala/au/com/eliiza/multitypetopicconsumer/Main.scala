package au.com.eliiza.multitypetopicconsumer

import au.com.eliiza.common.{CommonMain, Config, Consumer}
import au.com.eliiza.model._
import org.slf4j.{Logger, LoggerFactory}

object Main extends CommonMain {

  private lazy val logger: Logger = LoggerFactory.getLogger(this.getClass())

  override def main(conf: Config): Unit = {
    val consumer = new Consumer[UserCreateEvent](conf, processFn)
    consumer.consume(conf.default.getString("kafka.topic"))
  }

  def processFn[V](key: String, value: V): Either[Exception, Any] =
    Right(logger.info(s"consumed: key='$key' value=$value"))

}
