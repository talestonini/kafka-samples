package au.com.eliiza.multitypetopicproducer

import com.typesafe.config.{ConfigFactory, ConfigException}
import scala.util.{Try, Success, Failure}

final case class Args(env: String)

final case class Config(
  serverIp: String, serverPort: Int, kafkaBootstrapServers: String, schemaRegistryUrl: String,
  extraKafkaPropsFromFile: String, topic: String
)

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
          serverIp = conf.getString("server.ip"),
          serverPort = conf.getInt("server.port"),
          kafkaBootstrapServers = conf.getString("kafka.bootstrap-servers"),
          schemaRegistryUrl = conf.getString("kafka.schema-registry-url"),
          extraKafkaPropsFromFile =
            if (conf.hasPath("kafka.extra-props-from-file")) conf.getString("kafka.extra-props-from-file") else "",
          topic = conf.getString("kafka.topic")
        )
      )
    } catch {
      case e: ConfigException => Failure(e)
    }

}
