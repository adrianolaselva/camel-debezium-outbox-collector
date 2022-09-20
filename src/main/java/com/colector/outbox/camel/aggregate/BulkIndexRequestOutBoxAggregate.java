package com.colector.outbox.camel.aggregate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.apache.kafka.connect.data.Struct;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static java.lang.String.format;

@Component
public class BulkIndexRequestOutBoxAggregate implements AggregationStrategy {

    private static final String INDEX_PATTERN_FORMAT = "yyyy-MM-dd";
    private static final DateTimeFormatter indexPatternFormatter = DateTimeFormatter
        .ofPattern(INDEX_PATTERN_FORMAT)
        .withZone(ZoneId.from(ZoneOffset.UTC));

    @SneakyThrows
    @Override
    public Exchange aggregate(final Exchange oldExchange, final Exchange newExchange) {
        final var bodyStruct = newExchange.getIn().getBody(Struct.class);
        final var bulkRequest = oldExchange != null ? oldExchange.getIn().getBody(BulkRequest.class) : new BulkRequest();
        final var indexRequest = new IndexRequest(buildIndexNameByStruct(bodyStruct))
            .id(bodyStruct.get("id").toString())
            .type("_doc")
            .source(buildJsonSourceByStruct(bodyStruct), XContentType.JSON);

        bulkRequest.add(indexRequest);
        newExchange.getIn().setBody(bulkRequest);

        return newExchange;
    }

    private String buildIndexNameByStruct(final Struct bodyStruct) {
        final var createdAt = new Timestamp(bodyStruct.getInt64("created_at")).toInstant();
        return format("events-%s", indexPatternFormatter.format(createdAt));
    }

    private String buildJsonSourceByStruct(final Struct bodyStruct) throws JsonProcessingException {
        final var mapper = new ObjectMapper();
        final var event = mapper.createObjectNode();
        event.put("message", bodyStruct.get("message").toString());
        event.put("created_at", bodyStruct.get("created_at").toString());

        return mapper.writeValueAsString(event);
    }
}
