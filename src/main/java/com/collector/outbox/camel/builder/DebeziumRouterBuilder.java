package com.collector.outbox.camel.builder;

import com.collector.outbox.properties.entities.DebeziumRoutes;
import lombok.AllArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;

import static java.lang.String.format;

@AllArgsConstructor
public abstract class DebeziumRouterBuilder extends RouteBuilder {

    private final DebeziumRoutes debeziumRouters;

    @Override
    public abstract void configure() throws Exception;

    protected RouteDefinition fromDebezium(final String connectorName) throws Exception {
        if (!debeziumRouters.containsKey(connectorName)) {
            throw new Exception(format("connector %s not defined", connectorName));
        }

        return from(debeziumRouters.get(connectorName))
            .routeId(connectorName);
    }

}
