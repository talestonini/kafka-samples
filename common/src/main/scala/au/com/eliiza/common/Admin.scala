package au.com.eliiza.common

import gigahorse.support.okhttp.Gigahorse
import java.nio.charset.StandardCharsets
import org.apache.kafka.clients, org.apache.kafka.clients.admin.{AdminClientConfig, NewTopic}
import org.slf4j.{Logger, LoggerFactory}
import scala.concurrent.Await, scala.concurrent.duration._
import scala.io.Source
import scala.jdk.CollectionConverters._

class Admin(conf: Config) {

  protected lazy val logger: Logger = LoggerFactory.getLogger(this.getClass())

  private val admin = clients.admin.Admin.create(kafkaProps().asJava)

  def close(): Unit = {
    logger.info(s">>> closing kafka admin")
    admin.close()
  }

  def createTopics(names: String*): Unit = {
    logger.info(s">>> creating topics: ${names.mkString(", ")}")
    val newTopics = names.map(n => new NewTopic(n, 1, 1.toShort))
    admin.createTopics(newTopics.asJava)
  }

  def registerSchema(subjectName: String, avroSchemaRes: String, referencesRes: Option[String] = None): Unit = {
    val schemaRegistryUrl = conf.default.getString("kafka.schema-registry-url")
    val subjectUrl        = s"$schemaRegistryUrl/subjects/$subjectName/versions"
    val avroSchema = Source.fromResource(avroSchemaRes).toList.mkString.replaceAll("\"", "\\\\\"").replaceAll("\n", "")
    val payload =
      if (referencesRes.isEmpty) {
        s"""{"schema":"$avroSchema"}"""
      } else {
        val references = Source.fromResource(referencesRes.get).toList.mkString.replaceAll("\n", "")
        s"""{"schema":"$avroSchema","references":$references}"""
      }
    logger.info(s">>> registering schema: $subjectUrl")
    logger.info(s">>> $payload")
    Gigahorse.withHttp(Gigahorse.config) { http =>
      val post = Gigahorse
        .url(subjectUrl)
        .addHeader(("Content-Type", "application/vnd.schemaregistry.v1+json"))
        .post(payload, StandardCharsets.UTF_8)
      val run = http.run(post, Gigahorse.asString)
      val res = Await.result(run, 1.second)
      logger.info(s">>> registered schema: $res")
    }
  }

  private def kafkaProps(): Map[String, Any] = {
    val map = Map(
      AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG -> conf.default.getString("kafka.bootstrap-servers")
    )
    logger.debug(s">>> all admin kafka props: $map")
    map
  }

}
