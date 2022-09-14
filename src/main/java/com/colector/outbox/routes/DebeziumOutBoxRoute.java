package com.colector.outbox.routes;


import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import static java.lang.String.format;

@Slf4j
@Component
public class DebeziumOutBoxRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        var parameters = new StringBuilder();
        parameters.append(format("databaseHostname=%s", "127.0.0.1"));
        parameters.append(format("&databasePort=%s", "3306"));
        parameters.append(format("&databaseUser=%s", "root"));
        parameters.append(format("&databasePassword=%s", "root"));
        parameters.append(format("&databaseServerId=%s", "1"));
        parameters.append(format("&databaseServerName=%s", "collector_outbox"));
        parameters.append(format("&databaseIncludeList=%s", "outbox"));

        parameters.append(format("&databaseHistory=%s", "io.debezium.relational.history.KafkaDatabaseHistory"));
        parameters.append(format("&databaseHistoryKafkaBootstrapServers=%s", "127.0.0.1:9092"));

        parameters.append(format("&databaseHistoryKafkaTopic=%s", "debezium-outbox-db-history"));

//        parameters.append(format("&databaseHistorySkipUnparseableDdl=%s", "true"));
//        parameters.append(format("&databaseHistoryStoreOnlyMonitoredTablesDdl=%s", "true"));
//        parameters.append(format("&databaseHistoryStoreOnlyCapturedTablesDdl=%s", "true"));

        parameters.append(format("&tableIncludeList=%s", "outbox.outbox"));
        parameters.append(format("&binaryHandlingMode=%s", "base64"));
        parameters.append(format("&includeSchemaChanges=%s", "false"));
        parameters.append(format("&snapshotMode=%s", "when_needed"));
        parameters.append(format("&snapshotLockingMode=%s", "none"));
        parameters.append(format("&offsetStorage=%s", "org.apache.kafka.connect.storage.KafkaOffsetBackingStore"));
        parameters.append(format("&offsetStorageTopic=%s", "my_connect_offsets"));

        parameters.append(format("&additionalProperties.client.id=%s", "debezium"));
        parameters.append(format("&additionalProperties.group.id=%s", "debezium"));
        parameters.append(format("&additionalProperties.bootstrap.servers=%s", "127.0.0.1:9092"));
        parameters.append(format("&additionalProperties.database.history.kafka.topic=%s", "my_connect_configs"));
        parameters.append(format("&additionalProperties.config.storage.topic=%s", "my_connect_configs"));
        parameters.append(format("&additionalProperties.offset.storage.topic=%s", "my_connect_offsets"));
        parameters.append(format("&additionalProperties.status.storage.topic=%s", "my_connect_statuses"));

        parameters.append(format("&additionalProperties.config.storage.replication.factor=%s", "-1"));
        parameters.append(format("&additionalProperties.offset.storage.replication.factor=%s", "-1"));
        parameters.append(format("&additionalProperties.status.storage.replication.factor=%s", "-1"));

        parameters.append(format("&additionalProperties.connect.key.converter.schemas.enable=%s", "false"));
        parameters.append(format("&additionalProperties.connect.value.converter.schemas.enable=%s", "false"));
        parameters.append(format("&additionalProperties.connect.scheduled.rebalance.max.delay.ms=%s", "false"));
        parameters.append(format("&bridgeErrorHandler=%s", "true"));

        from(format("debezium-mysql:connector?%s", parameters))
            .log("body: ${body}");

    }

}
