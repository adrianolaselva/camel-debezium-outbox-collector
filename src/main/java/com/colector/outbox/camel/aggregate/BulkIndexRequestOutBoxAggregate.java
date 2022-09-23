package com.colector.outbox.camel.aggregate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.apache.kafka.connect.data.Struct;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static java.lang.String.format;

@Component
public class BulkIndexRequestOutBoxAggregate implements AggregationStrategy {

    private static final String INDEX_PATTERN_FORMAT = "yyyy-MM-dd";
    private static final String DATETIME_PATTERN_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    private static final DateTimeFormatter indexPatternFormatter = DateTimeFormatter
        .ofPattern(INDEX_PATTERN_FORMAT)
        .withZone(ZoneId.from(ZoneOffset.UTC));
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter
        .ofPattern(DATETIME_PATTERN_FORMAT)
        .withZone(ZoneId.from(ZoneOffset.UTC));
    private final ObjectMapper mapper = new ObjectMapper();

    @SneakyThrows
    @Override
    public Exchange aggregate(final Exchange oldExchange, final Exchange newExchange) {
        final var bodyStruct = newExchange.getIn().getBody(Struct.class);
        final var createdAt = timestampToInstant(bodyStruct.getInt64("created_at"));
        final var bulkRequest = oldExchange != null ? oldExchange.getIn().getBody(BulkRequest.class) : new BulkRequest();
        final var indexRequest = new IndexRequest(buildIndexNameByStruct(createdAt))
            .id(bodyStruct.get("id").toString())
            .type("_doc")
            .source(buildJsonSourceByStruct(bodyStruct, createdAt), XContentType.JSON);

        bulkRequest.add(indexRequest);
        newExchange.getIn().setBody(bulkRequest);

        return newExchange;
    }

    private String buildIndexNameByStruct(final Instant createdAt) {
        return format("events-%s", indexPatternFormatter.format(createdAt));
    }

    private String buildJsonSourceByStruct(final Struct bodyStruct, final Instant createdAt) throws IOException {
        final var objectNode = mapper.readValue(bodyStruct.get("message").toString(), ObjectNode.class);
        objectNode.put("id", bodyStruct.get("id").toString());
        objectNode.put("created_at", dateTimeFormatter.format(createdAt));

        return objectNode.toString();
    }

    private Instant timestampToInstant(final long timestamp) {
        return new Timestamp(timestamp).toInstant();
    }
}
