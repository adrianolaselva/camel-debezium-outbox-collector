package com.colector.outbox.properties;

import com.colector.outbox.entities.Settings;
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

    private String indexPrefix;
    private HashMap<String, Settings> connectors;

}

