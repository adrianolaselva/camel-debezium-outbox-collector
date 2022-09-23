package com.colector.outbox.camel.routes;


import com.colector.outbox.camel.aggregate.BulkIndexRequestOutBoxAggregate;
import com.colector.outbox.camel.builder.DebeziumRouterBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.component.elasticsearch.ElasticsearchComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DebeziumMySqlOutBoxRoute extends DebeziumRouterBuilder {

    @Override
    protected String getConnectorType() {
        return "debezium-mysql";
    }

    @Override
    protected String getConnectorName() {
        return "collector-outbox-mysql";
    }

    @Override
    protected String getRouteId() {
        return "debezium-mysql-route";
    }

    @Autowired
    private BulkIndexRequestOutBoxAggregate bulkIndexRequestOutBoxAggregate;

    @Autowired
    public ElasticsearchComponent elasticsearchComponent;

    @Override
    public void configure() throws Exception {
        getContext().addComponent("elasticsearch-rest", elasticsearchComponent);

        fromDebezium()
            .log("body: ${body}")
            .aggregate(constant(true), bulkIndexRequestOutBoxAggregate)
            .completionInterval(5_000)
            .completionSize(200)
            .to("elasticsearch-rest://docker-cluster?operation=Bulk");
    }
}
