import sbt._

object Dependencies {

  // versions
  lazy val kafkaVersion   = "3.4.0"
  lazy val logbackVersion = "1.2.11" // newest version not working; app fails to locate SLF4J impl at startup

  // libraries
  val avro                = "org.apache.avro"      % "avro"                  % "1.11.1"
  val gigahorse           = "com.eed3si9n"        %% "gigahorse-okhttp"      % "0.7.0"
  val jansi               = "org.fusesource.jansi" % "jansi"                 % "2.4.0" // formats logs with colors
  val kafkaAvroSerialiser = "io.confluent"         % "kafka-avro-serializer" % "7.3.3"
  val kafkaClients        = "org.apache.kafka"     % "kafka-clients"         % kafkaVersion
  val logbackClassic      = "ch.qos.logback"       % "logback-classic"       % logbackVersion
  val scalaTest           = "org.scalatest"       %% "scalatest"             % "3.2.15" % "test,it"
  val testContainers = "com.dimafeng" %% "testcontainers-scala-scalatest" % "0.40.12" % IntegrationTest
  val typesafeConfig = "com.typesafe"  % "config"                         % "1.4.2"

  // projects
  val basicDependencies = Seq(
    jansi,
    logbackClassic,
    scalaTest
  )
  val kafkaDependencies = Seq(
    kafkaAvroSerialiser,
    kafkaClients
  )

}
