package com.colector.outbox.camel.aggregate;

import com.colector.outbox.config.CollectorOutBoxProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.apache.kafka.connect.data.Struct;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static java.lang.String.format;

@Component
public class BulkDeleteRequestOutBoxAggregate implements AggregationStrategy {

    @Autowired
    private CollectorOutBoxProperties collectorOutBoxProperties;
    private static final String INDEX_PATTERN_FORMAT = "yyyy-MM";
    private static final DateTimeFormatter indexPatternFormatter = DateTimeFormatter
        .ofPattern(INDEX_PATTERN_FORMAT)
        .withZone(ZoneId.from(ZoneOffset.UTC));

    @SneakyThrows
    @Override
    public Exchange aggregate(final Exchange oldExchange, final Exchange newExchange) {
        final var camelDebeziumKey = newExchange.getIn().getHeader("CamelDebeziumKey", Struct.class);
        final var camelDebeziumBefore = newExchange.getIn().getHeader("CamelDebeziumBefore", Struct.class);
        final var bulkRequest = oldExchange != null ? oldExchange.getIn().getBody(BulkRequest.class) : new BulkRequest();

        final var createdAt = timestampToInstant(camelDebeziumBefore.getInt64("created_at"));

        bulkRequest.add(new DeleteRequest(buildIndexNameByStruct(createdAt))
            .id(camelDebeziumKey.get("id").toString())
            .type("_doc"));

        newExchange.getIn().setBody(bulkRequest);

        return newExchange;
    }

    private String buildIndexNameByStruct(final Instant createdAt) {
        return format("%s-%s", collectorOutBoxProperties.getIndexPrefix(), indexPatternFormatter.format(createdAt));
    }

    private Instant timestampToInstant(final long timestamp) {
        return new Timestamp(timestamp).toInstant();
    }
}
