#!/bin/bash

export AVRO_DIR="../model/src/main/resources/avro"
./publish_schema.sh $AVRO_DIR/UserCreateEvent.avsc http://localhost:8081/subjects/user-create-event-value
./publish_schema.sh $AVRO_DIR/UserUpdateEvent.avsc http://localhost:8081/subjects/user-update-event-value
# The .avsc_ file used below has the particular "_" character at the end of its name just so the sbt-avrohugger plugin
# does not attempt to process it to generate a specific record case class.  That's because the plugin does not support
# top-level union Avro schema files.  The code also does not need the top-level type that the union represents, as
# the producers are leaf-type-dependent.
./publish_schema.sh $AVRO_DIR/UserEvent.avsc_ http://localhost:8081/subjects/user-event-value $AVRO_DIR/UserEventRefs.json
