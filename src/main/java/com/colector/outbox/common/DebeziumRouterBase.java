package com.colector.outbox.common;

import com.colector.outbox.config.CollectorOutBoxProperties;
import lombok.AllArgsConstructor;
import org.apache.camel.builder.RouteBuilder;

import static java.lang.String.format;

@AllArgsConstructor
public abstract class DebeziumRouterBase extends RouteBuilder {

    private final CollectorOutBoxProperties collectorOutBoxProperties;

    protected String getUriWithParameters() {
        final var parameters = new StringBuilder();
        collectorOutBoxProperties.getConnectors()
            .get(getConnectorName())
            .getProperties()
            .forEach((s, o) -> parameters.append(format("%s%s=%s", parameters.isEmpty() ? "" : "&", s, o)));

        collectorOutBoxProperties.getConnectors()
            .get(getConnectorName())
            .getConfig().forEach((s, o) -> parameters.append(format("%s%s=%s", parameters.isEmpty() ? "" : "&", s, o)));

        return format("debezium-mysql://connector?%s", parameters);
    }

    protected abstract String getConnectorName();

    @Override
    public abstract void configure() throws Exception;

}
