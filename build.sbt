import Dependencies._

ThisBuild / scalaVersion := "2.13.10"
ThisBuild / organization := "au.com.eliiza"

ThisBuild / scalacOptions += "-deprecation"

ThisBuild / assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x                             => MergeStrategy.first
}

ThisBuild / coverageEnabled            := true
ThisBuild / coverageFailOnMinimum      := true
ThisBuild / coverageMinimumStmtTotal   := 85
ThisBuild / coverageMinimumBranchTotal := 50

lazy val kafkaSamples = (project in file("."))
  .aggregate(model, common, multiTypeTopicProducer, multiTypeTopicConsumer)
  .configs(IntegrationTest)
  .dependsOn(model, common, multiTypeTopicProducer, multiTypeTopicConsumer)
  .enablePlugins(JavaAppPackaging)
  .settings(
    name    := "kafka-samples",
    version := "0.1.0",
    libraryDependencies ++= Seq(
      scalaTest
    )
  )

lazy val model = project
  .settings(
    name    := "model",
    version := "0.1.0",
    libraryDependencies ++= Seq(avro),
    Compile / avroSpecificSourceDirectories := Seq(baseDirectory.value / "src/main/resources/avro"),
    Compile / sourceGenerators += (Compile / avroScalaGenerateSpecific).taskValue,
    coverageExcludedPackages := "au\\.com\\.eliiza\\.model\\..*"
  )

lazy val common = project
  .configs(IntegrationTest)
  .dependsOn(model)
  .settings(
    name    := "common",
    version := "0.1.0",
    resolvers += "confluent" at "https://packages.confluent.io/maven",
    libraryDependencies ++= basicDependencies ++ kafkaDependencies ++ Seq(gigahorse, testContainers, typesafeConfig),
    Defaults.itSettings,
    IntegrationTest / fork := true
  )

lazy val multiTypeTopicProducer = project
  .configs(IntegrationTest)
  .dependsOn(model, common % "compile;it->it")
  .settings(
    name    := "multi-type-topic-producer",
    version := "0.1.0",
    Defaults.itSettings,
    IntegrationTest / fork := true
  )

lazy val multiTypeTopicConsumer = project
  .configs(IntegrationTest)
  .dependsOn(model, common % "compile;it->it")
  .settings(
    name    := "multi-type-topic-consumer",
    version := "0.1.0",
    Defaults.itSettings,
    IntegrationTest / fork := true,
    coverageExcludedFiles  := ".*Main.*" // consumer runs in an infinite loop
  )
