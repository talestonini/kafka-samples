package au.com.eliiza.common

import au.com.eliiza.common.Config
import scala.util.{Success, Failure}
import org.slf4j.{Logger, LoggerFactory}

trait CommonMain {

  private lazy val logger: Logger = LoggerFactory.getLogger(this.getClass())

  def main(conf: Config): Unit = {}

  final def main(args: Array[String]): Unit =
    (for {
      profile <- Config.validateProfile(args(0))
      conf    <- Config.getConfig(profile)
    } yield conf) match {
      case Success(conf) =>
        logger.info(">>> starting app...")
        main(conf)
      case Failure(e) =>
        logger.info(s">>> unable to start app: ${e.getMessage()}")
    }

}
