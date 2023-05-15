package au.com.eliiza.common

import org.scalatest.TryValues._
import scala.util.{Success, Failure}

class ConfigSpec extends BaseSpec {

  "A Config" should "recognise valid execution profiles" in {
    Config.validProfiles.foreach { p =>
      Config.validateProfile(p).isSuccess shouldBe true
    }
  }

  it should "not recognise invalid execution profiles" in {
    Config.validateProfile("bla").isFailure shouldBe true
  }

  it should "load props from extra file if extra props file config entry is present" in {
    val conf: Config = (for {
      args <- Config.validateProfile("prod")
      conf <- Config.getConfig(args)
    } yield conf) match {
      case Success(c) => c
      case Failure(e) => throw new Exception(s"unable to load prod config: ${e.getMessage()}")
    }

    conf.extraAndOverride.contains("bootstrap-servers") shouldBe true
    conf.extraAndOverride.contains("secret-prop") shouldBe true
  }

  it should "not fail when the extra props file config entry is blank" in {
    val conf: Config = (for {
      args <- Config.validateProfile("test")
      conf <- Config.getConfig(args)
    } yield conf) match {
      case Success(c) => c
      case Failure(e) => throw new Exception(s"unable to load test config: ${e.getMessage()}")
    }

    conf.extraAndOverride shouldBe Map.empty
  }

  it should "not fail when the extra props file config entry is not present" in {
    val conf: Config = (for {
      args <- Config.validateProfile("local")
      conf <- Config.getConfig(args)
    } yield conf) match {
      case Success(c) => c
      case Failure(e) => throw new Exception(s"unable to load test config: ${e.getMessage()}")
    }

    conf.extraAndOverride shouldBe Map.empty
  }

  it should "fail when loading invalid extra props file" in {
    val conf = Config.getConfig("invalid-extra-props-file")
    conf.isFailure shouldBe true
  }

}
