#!/bin/bash

AVRO_SCHEMA_FILE=$1
SUBJECT_URL=$2
REFERENCES_FILE=$3

# create schema payload
cp ${AVRO_SCHEMA_FILE} schema
sed -i '' -e 's/\"/\\\"/g' schema
cp ${REFERENCES_FILE} refs

if [ -z "$3" ]
then
  echo '{"schema":"'$(cat schema)'"}' >> payload
else
  echo '{"schema":"'$(cat schema)'","references":'$(cat refs)'}' >> payload
fi

# publish the schema
curl -X POST \
     -H "Content-Type: application/vnd.schemaregistry.v1+json" \
     -d @payload \
     ${SUBJECT_URL}/versions

# delete temporary files
rm schema payload refs
