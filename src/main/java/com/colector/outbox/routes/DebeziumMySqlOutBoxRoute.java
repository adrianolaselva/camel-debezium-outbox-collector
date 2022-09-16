package com.colector.outbox.routes;


import com.colector.outbox.common.DebeziumRouterBase;
import com.colector.outbox.config.CollectorOutBoxProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DebeziumMySqlOutBoxRoute extends DebeziumRouterBase {

    public DebeziumMySqlOutBoxRoute(final CollectorOutBoxProperties collectorOutBoxProperties) {
        super(collectorOutBoxProperties);
    }

    @Override
    protected String getConnectorType() {
        return "debezium-mysql";
    }

    @Override
    protected String getConnectorName() {
        return "collector-outbox-mysql";
    }


    @Override
    public void configure() throws Exception {
        from(getUriWithParameters())
            .log("body: ${body}");
    }

}
