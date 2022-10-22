package com.collector.outbox.camel.process;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.kafka.connect.data.Struct;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.apache.camel.Exchange.EXCEPTION_CAUGHT;

@Component
public class ElasticsearchFailedProcessor implements Processor {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void process(final Exchange exchange) throws Exception {
        final var bodyStruct = exchange.getIn().getBody(Struct.class);
        final var headers = new HashMap<String, Object>();
        final var attributes = new HashMap<String, Object>();
        final var exception = exchange.getProperty(EXCEPTION_CAUGHT, Exception.class);

        exchange.getIn().getHeaders().forEach((key, value) -> headers.put(key, value == null ? "" : value.toString()));
        bodyStruct.schema().fields().forEach(field -> attributes.put(field.name(), bodyStruct.get(field.name())));

        final var sourceJson = mapper.writeValueAsString(Map.of(
            "headers", headers,
            "exception", exception.getMessage(),
            "body", attributes));

        final var indexRequest = new IndexRequest("exceptions")
            .id(UUID.randomUUID().toString())
            .type("_doc")
            .source(sourceJson, XContentType.JSON);

        exchange.getIn().setBody(indexRequest);
    }

}
