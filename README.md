# Kafka Samples

This collection of sample projects serve as documentation in the form of code for Kafka client applications.  Some of
them are just dependencies for all the samples, eg model and common, which contain data types (event types in Avro
schema definition) and common/utility code.

# Dependencies

We use the *Scala* language and the [SBT](https://www.scala-sbt.org/index.html) build tool.  Tests are mostly written in
*Scalatest*, but other frameworks are encouraged too.  *Integration Tests* utilise
[Test Containers](https://www.testcontainers.org/) (in fact,
[testcontainers-scala](https://index.scala-lang.org/testcontainers/testcontainers-scala)), so you'll need to have
*Docker*. In summary:
- Scala and SBT (recommended installation with [Coursier](https://get-coursier.io/))
- Docker

# SBT Life-cycle

    clean
    compile
    test
    IntegrationTest/test
    coverageAggregate        <--- for single report of all projects (use coverageReport if in single project*)
    assembly

\* You can work in a single project by running `project <project name>`, eg `project common`.

# Projects

## Model

Contains data model types, ie event types, defined as Avro schemas.  Plugin `sbt-avrohugger` generates Scala case
classes for those types at compile time.  The case classes are Avro Specific Records (ie extend `SpecificRecordBase`),
providing strong types for your Kafka code.

## Common

Contains common or utility code to be shared by all other projects.  It should be a dependency of the sample projects.

## Multi-type Topic Producer

This Kafka producer app demos how to produce multiple types of events into the same topic, when still using
`TopicNameStrategy` for the subject naming strategy.

## Multi-type Topic Consumer

This Kafka consumer app demos how to consume from multiple types of events in the same topic, when still using
`TopicNameStrategy` for the subject naming strategy.
