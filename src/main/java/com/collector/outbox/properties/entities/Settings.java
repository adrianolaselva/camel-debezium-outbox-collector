package com.collector.outbox.properties.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
public class Settings {

    private String connectorType;
    private HashMap<String, Object> properties;
    private HashMap<String, Object> config;
    private Target target;
}
