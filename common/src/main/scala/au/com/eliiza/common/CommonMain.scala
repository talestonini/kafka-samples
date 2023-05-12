package au.com.eliiza.common

import au.com.eliiza.common.Config
import scala.util.{Success, Failure}
import org.slf4j.{Logger, LoggerFactory}

trait CommonMain {

  private lazy val logger: Logger = LoggerFactory.getLogger(this.getClass())

  def main(conf: Config): Unit = {}

  final def main(args: Array[String]): Unit = {
    val config = for {
      args <- Config.validateArgs(args.toList)
      conf <- Config.getConfig(args)
    } yield conf

    config match {
      case Success(conf) =>
        logger.info(">>> starting app...")
        main(conf)
      case Failure(e) =>
        logger.info(s">>> unable to start app: ${e.getMessage()}")
    }
  }

}
