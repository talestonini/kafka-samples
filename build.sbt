ThisBuild / scalaVersion := "2.13.10"
ThisBuild / organization := "au.com.eliiza"

val avro                = "org.apache.avro"      % "avro"                  % "1.11.1"
val jansi               = "org.fusesource.jansi" % "jansi"                 % "2.4.0" // formats logs with colors
val kafkaAvroSerialiser = "io.confluent"         % "kafka-avro-serializer" % "7.3.3"
val kafkaClients        = "org.apache.kafka"     % "kafka-clients"         % "3.4.0"
val logbackClassic      = "ch.qos.logback"       % "logback-classic"       % "1.4.7"
val scalaTest           = "org.scalatest"       %% "scalatest"             % "3.2.15" % Test
val typesafeConfig      = "com.typesafe"         % "config"                % "1.4.2"

lazy val kafkaSamples = (project in file("."))
  .aggregate(util, model, multiTypeTopicProducer)
  .dependsOn(util, model, multiTypeTopicProducer)
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "Kafka Samples",
    libraryDependencies ++= Seq(
      scalaTest
    )
  )

lazy val util = project
  .settings(
    name    := "Util",
    version := "0.1.0"
  )

lazy val model = project
  .dependsOn(util % "test->test")
  .settings(
    name    := "Model",
    version := "0.1.0",
    libraryDependencies ++= Seq(
      avro
    )
  )

lazy val multiTypeTopicProducer = project
  .dependsOn(model, util % "test->test")
  .settings(
    name    := "Multi-type Topic Producer",
    version := "0.1.0",
    resolvers += "confluent" at "https://packages.confluent.io/maven",
    libraryDependencies ++= Seq(
      jansi,
      kafkaAvroSerialiser,
      kafkaClients,
      logbackClassic,
      scalaTest,
      typesafeConfig
    )
  )

Compile / avroSpecificSourceDirectories := Seq(baseDirectory.value / "model/src/main/resources/avro")
Compile / avroSpecificScalaSource       := baseDirectory.value / s"model/src/main/scala"
Compile / sourceGenerators += (Compile / avroScalaGenerateSpecific).taskValue
