package com.colector.outbox.camel.builder;

import com.colector.outbox.config.CollectorOutBoxProperties;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import static java.lang.String.format;

public abstract class DebeziumRouterBuilder extends RouteBuilder {

    @Autowired
    private CollectorOutBoxProperties collectorOutBoxProperties;

    private boolean enableDefaultErrorHandler;

    protected abstract String getConnectorType();

    protected abstract String getConnectorName();

    protected abstract String getRouteId();

    @Override
    public abstract void configure() throws Exception;

    protected void enableDefaultErrorHandler() {
        this.enableDefaultErrorHandler = true;
    }

    protected RouteDefinition fromDebezium() {
        if (this.enableDefaultErrorHandler) {
            buildDefaultErrorHandler();
        }

        return from(buildRouteDebeziumCamelUri())
            .routeId(getRouteId());
    }

    private String buildRouteDebeziumCamelUri() {
        final var parameters = new StringBuilder();
        collectorOutBoxProperties.getConnectors()
            .get(getConnectorName())
            .getProperties()
            .forEach((s, o) -> parameters.append(format("%s%s=%s", parameters.isEmpty() ? "" : "&", s, o)));

        collectorOutBoxProperties.getConnectors()
            .get(getConnectorName())
            .getConfig().forEach((s, o) -> parameters.append(format("%s%s=%s", parameters.isEmpty() ? "" : "&", s, o)));

        return format("%s://connector?%s", getConnectorType(), parameters);
    }

    private void buildDefaultErrorHandler() {
        onException(Exception.class)
            .handled(true)
            .end();
    }

}
