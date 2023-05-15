package au.com.eliiza.common

import com.dimafeng.testcontainers.{DockerComposeContainer, ExposedService}
import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import java.io.File
import org.scalatest._, flatspec._, matchers._
import org.slf4j.{Logger, LoggerFactory}
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import scala.util.{Success, Failure}

trait BaseItSpec
    extends AnyFlatSpec with should.Matchers with BeforeAndAfterAll with TestContainerForAll with SetupKafka {

  protected lazy val logger: Logger = LoggerFactory.getLogger(this.getClass())

  override val containerDef: DockerComposeContainer.Def =
    DockerComposeContainer.Def(
      new File("src/it/resources/compose-test.yml"),
      exposedServices = Seq(
        ExposedService("zookeeper", 2181, new LogMessageWaitStrategy().withRegEx(".*PrepRequestProcessor.*started.*")),
        ExposedService("kafka-broker", 9092,
          new LogMessageWaitStrategy().withRegEx(".*started \\(kafka\\.server\\.KafkaServer\\).*")),
        ExposedService("schema-registry", 8081,
          new LogMessageWaitStrategy().withRegEx(".*Server started, listening for requests.*"))
      )
    )

  val conf = Config.getConfig("test") match {
    case Success(c) => c
    case Failure(e) => throw new Exception(s"unable to load test config: ${e.getMessage()}")
  }

  override protected def beforeAll(): Unit =
    setupKafka("test")

}
