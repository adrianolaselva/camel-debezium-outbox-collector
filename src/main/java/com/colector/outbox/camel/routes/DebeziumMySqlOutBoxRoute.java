package com.colector.outbox.camel.routes;


import com.colector.outbox.camel.aggregate.BulkDeleteRequestOutBoxAggregate;
import com.colector.outbox.camel.aggregate.BulkIndexRequestOutBoxAggregate;
import com.colector.outbox.camel.builder.DebeziumRouterBuilder;
import com.colector.outbox.camel.process.ElasticsearchFailedProcessor;
import io.debezium.data.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.component.elasticsearch.ElasticsearchComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.lang.String.format;
import static org.apache.camel.LoggingLevel.ERROR;
import static org.apache.camel.LoggingLevel.INFO;
import static org.apache.camel.LoggingLevel.WARN;

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
    private BulkDeleteRequestOutBoxAggregate bulkDeleteRequestOutBoxAggregate;

    @Autowired
    private ElasticsearchFailedProcessor elasticsearchFailedProcessor;

    @Autowired
    public ElasticsearchComponent elasticsearchComponent;

    @Override
    public void configure() throws Exception {
        onException(Exception.class)
            .handled(true)
            .log(ERROR, "failed to process event: ${header.CamelDebeziumKey} => ${body}")
            .to("direct:elasticsearch-failed")
            .end();

        getContext().addComponent("elasticsearch-rest", elasticsearchComponent);

        fromDebezium()
            .choice()
            .when(simple(format("${header.CamelDebeziumOperation} == '%s'", Envelope.Operation.DELETE.code())))
                .to("direct:elasticsearch-bulk-delete")
            .when(simple(format("${header.CamelDebeziumOperation} == '%s'", Envelope.Operation.CREATE.code())))
                .to("direct:elasticsearch-bulk-insert")
            .when(simple(format("${header.CamelDebeziumOperation} == '%s'", Envelope.Operation.UPDATE.code())))
                .to("direct:elasticsearch-bulk-update")
            .otherwise()
                .log(WARN, "ignore operation: ${header.CamelDebeziumOperation}");

        from("direct:elasticsearch-bulk-insert")
            .routeId("direct:elasticsearch-bulk-insert")
            .threads(1, 10)
            .aggregate(constant(true), bulkIndexRequestOutBoxAggregate)
            .completionInterval(2_000)
            .completionSize(200)
            .to("elasticsearch-rest://docker-cluster?operation=Bulk");

        from("direct:elasticsearch-bulk-update")
            .routeId("direct:elasticsearch-bulk-update")
            .threads(1, 10)
            .aggregate(constant(true), bulkIndexRequestOutBoxAggregate)
            .completionInterval(5_000)
            .completionSize(200)
            .to("elasticsearch-rest://docker-cluster?operation=Bulk");

        from("direct:elasticsearch-bulk-delete")
            .routeId("direct:elasticsearch-bulk-delete")
            .log(INFO, "remove row id: ${header.CamelDebeziumKey}")
            .threads(1, 2)
            .aggregate(constant(true), bulkDeleteRequestOutBoxAggregate)
            .completionInterval(2_000)
            .completionSize(20)
            .to("elasticsearch-rest://docker-cluster?operation=Bulk");

        from("direct:elasticsearch-truncate")
            .routeId("direct:elasticsearch-truncate")
            .log(INFO, "truncate table: ${header.CamelDebeziumKey}");

        from("direct:elasticsearch-failed")
            .routeId("direct:exception")
            .process(elasticsearchFailedProcessor)
            .to("elasticsearch-rest://docker-cluster?operation=Index");
    }
}
