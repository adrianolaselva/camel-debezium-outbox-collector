package com.colector.outbox.camel.routes;


import com.colector.outbox.camel.builder.DebeziumRouterBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.data.Struct;
import org.springframework.stereotype.Component;

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

            });
    }

}
