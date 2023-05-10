package au.com.eliiza.common

import com.typesafe.config.{ConfigFactory, ConfigException}
import scala.io.Source
import scala.util.{Try, Success, Failure}

final case class Args(env: String)

final case class Config(default: com.typesafe.config.Config, extraAndOverride: Map[String, Any])

trait BootstrapConfig[M[_]] {
  def validateArgs(args: List[String]): M[Args]
  def getConfig(args: Args): M[Config]
}

object Config extends BootstrapConfig[Try] {

  private val validEnvs = List("local", "test", "prod")

  override def validateArgs(args: List[String]): Try[Args] =
    args match {
      case List(env) =>
        if (validEnvs.contains(env))
          Success(Args(env))
        else
          Failure(new Exception(s"environment must be one of [${validEnvs.mkString(", ")}]"))
      case _ => Failure(new Exception("wrong number of arguments"))
    }

  override def getConfig(args: Args): Try[Config] =
    try {
      val conf = ConfigFactory.load().getConfig(args.env)
      Success(
        Config(
          default = conf,
          extraAndOverride =
            if (conf.hasPath("kafka.extra-props-file")) loadProps(conf.getString("kafka.extra-props-file"))
            else Map.empty
        )
      )
    } catch {
      case e: ConfigException => Failure(e)
    }

  private def loadProps(file: String): Map[String, Any] =
    if (file.isEmpty())
      Map.empty
    else
      (for {
        line <- Source.fromFile(file).getLines()
        if !line.trim().isEmpty() && !line.trim().startsWith("#")
        entry = line.split("=", 2)
      } yield (entry(0) -> entry(1))).toMap

}
