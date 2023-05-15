package au.com.eliiza.common

import org.scalatest.TryValues._
import scala.util.Try
import scala.util.{Success, Failure}

class CommonMainSpec extends BaseSpec {

  "A CommonMain" should "validate command line arguments and load app config" in {
    var testConf: Try[Config] = Failure(new Exception())
    val app = new CommonMain() {
      override def main(conf: Config): Unit = {
        super.main(conf)
        testConf = Success(conf)
      }
    }
    app.main(Array("local"))

    testConf.isSuccess shouldBe true
  }

  it should "report failed app initialisation" in {
    var testConf: Try[Config] = Failure(new Exception())
    val app = new CommonMain() {
      override def main(conf: Config): Unit = {
        super.main(conf)
        testConf = Success(conf)
      }
    }
    app.main(Array("bla"))

    testConf.isFailure shouldBe true
  }

}
