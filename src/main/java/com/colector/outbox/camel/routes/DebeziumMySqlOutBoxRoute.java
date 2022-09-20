package com.colector.outbox.camel.routes;


import com.colector.outbox.camel.aggregate.BulkIndexRequestOutBoxAggregate;
import com.colector.outbox.camel.builder.DebeziumRouterBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.data.Struct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.lang.String.format;

@Slf4j
@Component
public class DebeziumMySqlOutBoxRoute extends DebeziumRouterBuilder {

    @Override
    protected String getConnectorType() {
        return "debezium-mysql";
    }

    @Override
    protected String getConnectorName() {
        return "collector-outbox-mysql";
    }

    @Override
    protected String getRouteId() {
        return "debezium-mysql-route";
    }

    @Autowired
    private BulkIndexRequestOutBoxAggregate bulkIndexRequestOutBoxAggregate;

    @Override
    public void configure() throws Exception {
        fromDebezium()
            .log("headers: ${headers}")
            .log("body: ${body}")
            .process(exchange -> {
                final var bodyValue = exchange.getIn().getBody(Struct.class);
                bodyValue.schema()
                    .fields()
                    .forEach(field -> log.info("field: {}", field));
            })
            .aggregate(constant(true), bulkIndexRequestOutBoxAggregate)
            .completionInterval(5_000)
            .toD(buildElasticToUri());
    }

    private String buildElasticToUri() {
        return format("elasticsearch-rest://%s?operation=Bulk&hostAddresses=RAW(%s:%s)&enableSSL=true",
            "opensearch-cluster", "127.0.0.1", "9200");
    }

//    private String buildElasticToUri() {
//        return format("elasticsearch-rest://%s?operation=Bulk&hostAddresses=%s:%s&enableSSL=false",
//            "opensearch-cluster", "127.0.0.1", "9200");
//    }
}
