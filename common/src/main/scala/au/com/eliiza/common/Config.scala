package au.com.eliiza.common

import com.typesafe.config.{ConfigFactory, ConfigException}
import scala.io.Source
import scala.util.{Try, Success, Failure}
import org.slf4j.{Logger, LoggerFactory}

final case class Config(default: com.typesafe.config.Config, extraAndOverride: Map[String, Any])

object Config {

  val validProfiles = List("local", "test", "prod")

  def validateProfile(profile: String): Try[String] =
    if (validProfiles.contains(profile))
      Success(profile)
    else
      Failure(new Exception(s">>> profile must be one of [${validProfiles.mkString(", ")}]"))

  def getConfig(profile: String): Try[Config] =
    try {
      val conf = ConfigFactory.load().getConfig(profile)
      Success(
        Config(
          default = conf,
          extraAndOverride =
            if (conf.hasPath("kafka.extra-props-file")) loadProps(conf.getString("kafka.extra-props-file"))
            else Map.empty
        )
      )
    } catch {
      case e: Exception => Failure(e)
    }

  private def loadProps(file: String): Map[String, Any] =
    if (file.isBlank())
      Map.empty
    else
      (for {
        line <- Source.fromFile(file).getLines()
        if !line.trim().isEmpty() && !line.trim().startsWith("#")
        entry = line.split("=", 2)
      } yield (entry(0) -> entry(1))).toMap

}
