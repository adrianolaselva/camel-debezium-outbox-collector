package com.colector.outbox.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "collector-outbox")
public class CollectorOutBoxProperties {

    private HashMap<String, Settings> connectors;

    @Getter
    @Setter
    public static class Settings {

        private HashMap<String, Object> properties;

        private HashMap<String, Object> config;
    }
}

