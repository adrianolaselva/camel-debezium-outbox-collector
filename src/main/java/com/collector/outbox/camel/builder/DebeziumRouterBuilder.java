package com.collector.outbox.camel.builder;

import com.collector.outbox.properties.entities.DebeziumRoutes;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import static java.lang.String.format;

public abstract class DebeziumRouterBuilder extends RouteBuilder {

    @Autowired
    private DebeziumRoutes debeziumRouters;

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
