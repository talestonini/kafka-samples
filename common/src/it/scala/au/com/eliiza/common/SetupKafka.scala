package au.com.eliiza.common

import org.slf4j.{Logger, LoggerFactory}
import scala.util.{Success, Failure}

trait SetupKafka {

  private lazy val logger: Logger = LoggerFactory.getLogger(this.getClass())

  def setupKafka(profile: String): Unit = {
    (for {
      profile <- Config.validateProfile(profile)
      conf    <- Config.getConfig(profile)
    } yield conf) match {
      case Success(conf) =>
        logger.info(">>> setting up Kafka...")
        val admin = new Admin(conf)
        try {
          createTopics(admin)
          registerSchemas(admin)
        } finally {
          admin.close()
        }
      case Failure(e) =>
        logger.info(s">>> unable to setup Kafka: ${e.getMessage()}")
    }
  }

  def createTopics(admin: Admin): Unit

  def registerSchemas(admin: Admin): Unit

}
