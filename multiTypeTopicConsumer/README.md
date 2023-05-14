# Multi-type Topic Consumer

This Kafka consumer app demos how to consume from multiple types of events in the same topic, when still using
`TopicNameStrategy` for the subject naming strategy.  It assumes the use of the Confluent Schema Registry and the
Confluent Kafka Avro serialiser.

For that to work, the following consumer Kafka properties are necessary:
- `auto.register.schemas=false`
- `use.latest.version=true`

The demo uses these model types:

            ----------------
            |  User Event  |
            | (Avro union) |
            ----------------
                    A
                    |
           -------------------
           |                 |
    ---------------   ---------------
    | User Create |   | User Update |
    |    Event    |   |    Event    |
    ---------------   ---------------

All types must be registered in the Schema Registry beforehand (check script `register_schemas_local.sh`).  The child
types must have `sbt-avrohugger` generate Scala case classes from their `.avsc` file, but not the parent type, which is
not needed by the code and not supported by the plugin (it's simply an Avro union of the child types).

References:
- https://www.confluent.io/blog/put-several-event-types-kafka-topic/
- https://www.confluent.io/blog/multiple-event-types-in-the-same-kafka-topic/
