package au.com.eliiza.common

import org.scalatest._, flatspec._, matchers._
import org.slf4j.{Logger, LoggerFactory}
import scala.util.{Success, Failure}

trait BaseSpec extends AnyFlatSpec with should.Matchers with BeforeAndAfterAll {

  protected lazy val logger: Logger = LoggerFactory.getLogger(this.getClass())

  val conf = Config.getConfig("test") match {
    case Success(c) => c
    case Failure(e) => throw new Exception(s"unable to load test config: ${e.getMessage()}")
  }

}
