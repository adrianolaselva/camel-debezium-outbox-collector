package com.collector.outbox.config;

import com.collector.outbox.properties.entities.DebeziumRoutes;
import com.collector.outbox.properties.entities.Settings;
import com.collector.outbox.properties.CollectorOutBoxProperties;
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
            .forEach((key, value) -> {
                try {
                    debeziumRoutes.put(key, buildRouteDebeziumCamelUri(key, value));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

        return debeziumRoutes;
    }

    private String buildRouteDebeziumCamelUri(final String connectorName, final Settings settings) throws Exception {
        final var parameters = new StringBuilder();
        if (settings.getProperties().isEmpty()) {
            throw new Exception(format("connector '%s' properties not defined", connectorName));
        }

        settings.getProperties()
            .forEach((s, o) -> parameters.append(format("%s%s=%s", parameters.isEmpty() ? "" : "&", s, o)));

        if (settings.getConfig() != null) {
            settings.getConfig().forEach((s, o) -> parameters.append(format("%s%s=%s", parameters.isEmpty() ? "" : "&", s, o)));
        }

        return format("%s://connector?%s", settings.getConnectorType(), parameters);
    }
}
