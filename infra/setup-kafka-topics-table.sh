#!/bin/sh
set -e

docker exec -it kafka.outbox.collector.dev kafka-topics.sh \
         --zookeeper zookeeper.outbox.collector.dev:2181 \
         --create --topic my_connect_offsets \
         --replication-factor 1 --partitions 1 \
         --if-not-exists --config "cleanup.policy=compact" \
         --config "retention.ms=43200000"

docker exec -it kafka.outbox.collector.dev kafka-topics.sh \
         --zookeeper zookeeper.outbox.collector.dev:2181 \
         --create --topic my_connect_configs \
         --replication-factor 1 --partitions 1 \
         --if-not-exists --config "cleanup.policy=compact" \
         --config "retention.ms=43200000"

docker exec -it kafka.outbox.collector.dev kafka-topics.sh \
         --zookeeper zookeeper.outbox.collector.dev:2181 \
         --create --topic my_connect_statuses \
         --replication-factor 1 --partitions 1 \
         --if-not-exists --config "cleanup.policy=compact" \
         --config "retention.ms=43200000"

docker exec -it kafka.outbox.collector.dev kafka-topics.sh \
         --zookeeper zookeeper.outbox.collector.dev:2181 \
         --create --topic debezium-outbox-db-history \
          --replication-factor 1 --partitions 1 \
          --if-not-exists --config "retention.ms=157680000000"

docker exec -it kafka.outbox.collector.dev kafka-topics.sh \
         --zookeeper zookeeper.outbox.collector.dev:2181 \
         --create --topic debezium-outbox-db-history \
          --replication-factor 1 --partitions 1 \
          --if-not-exists --config "retention.ms=157680000000"
