package com.colector.outbox.config;

import com.colector.outbox.entities.DebeziumRoutes;
import com.colector.outbox.entities.Settings;
import com.colector.outbox.properties.CollectorOutBoxProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.lang.String.format;

@Configuration
public class DebeziumConfig {

    @Autowired
    private CollectorOutBoxProperties collectorOutBoxProperties;

    @Bean
    public DebeziumRoutes debeziumRouters() {
        var debeziumRoutes = new DebeziumRoutes();
        collectorOutBoxProperties.getConnectors()
            .forEach((key, value) -> debeziumRoutes.put(key, buildRouteDebeziumCamelUri(key, value)));

        return debeziumRoutes;
    }

    private String buildRouteDebeziumCamelUri(final String connectorName, final Settings settings) {
        final var parameters = new StringBuilder();

        collectorOutBoxProperties.getConnectors()
            .get(connectorName)
            .getProperties()
            .forEach((s, o) -> parameters.append(format("%s%s=%s", parameters.isEmpty() ? "" : "&", s, o)));

        collectorOutBoxProperties.getConnectors()
            .get(connectorName)
            .getConfig().forEach((s, o) -> parameters.append(format("%s%s=%s", parameters.isEmpty() ? "" : "&", s, o)));

        return format("%s://connector?%s", settings.getConnectorType(), parameters);
    }
}
