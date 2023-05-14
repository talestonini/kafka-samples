#!/bin/bash

TOPIC_NAME=$1

# find out cluster id
curl -X GET \
     -H "Content-Type: application/json" \
     http://localhost:8082/v3/clusters >> clusters
sed -nr 's/.*"cluster_id":"([a-zA-Z0-9]*)".*/\1/p' clusters >> cluster_id

# publish the schema
curl -X POST \
     -H "Content-Type: application/json" \
     -d '{"topic_name":"'$TOPIC_NAME'"}' \
     http://localhost:8082/v3/clusters/$(cat cluster_id)/topics

rm clusters cluster_id
