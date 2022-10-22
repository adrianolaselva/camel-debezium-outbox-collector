package com.collector.outbox.camel.routes;


import com.collector.outbox.camel.aggregate.BulkDeleteRequestOutBoxAggregate;
import com.collector.outbox.camel.aggregate.BulkIndexRequestOutBoxAggregate;
import com.collector.outbox.camel.builder.DebeziumRouterBuilder;
import com.collector.outbox.camel.process.ElasticsearchFailedProcessor;
import com.collector.outbox.properties.entities.DebeziumRoutes;
import io.debezium.data.Envelope;
import lombok.AllArgsConstructor;
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

    private final BulkIndexRequestOutBoxAggregate bulkIndexRequestOutBoxAggregate;
    private final BulkDeleteRequestOutBoxAggregate bulkDeleteRequestOutBoxAggregate;
    private final ElasticsearchFailedProcessor elasticsearchFailedProcessor;
    public final ElasticsearchComponent elasticsearchComponent;

    public DebeziumMySqlOutBoxRoute(final DebeziumRoutes debeziumRouters,
        final BulkIndexRequestOutBoxAggregate bulkIndexRequestOutBoxAggregate,
        final BulkDeleteRequestOutBoxAggregate bulkDeleteRequestOutBoxAggregate,
        final ElasticsearchFailedProcessor elasticsearchFailedProcessor, final ElasticsearchComponent elasticsearchComponent) {
        super(debeziumRouters);
        this.bulkIndexRequestOutBoxAggregate = bulkIndexRequestOutBoxAggregate;
        this.bulkDeleteRequestOutBoxAggregate = bulkDeleteRequestOutBoxAggregate;
        this.elasticsearchFailedProcessor = elasticsearchFailedProcessor;
        this.elasticsearchComponent = elasticsearchComponent;
    }

    @Override
    public void configure() throws Exception {
        onException(Exception.class)
            .handled(true)
            .log(ERROR, "failed to process event: ${header.CamelDebeziumKey} => ${body}")
            .to("direct:elasticsearch-failed")
            .end();

        getContext().addComponent("elasticsearch-rest", elasticsearchComponent);

        fromDebezium("collector-outbox-mysql")
            .choice()
            .when(simple(format("${header.CamelDebeziumOperation} == '%s'", Envelope.Operation.DELETE.code())))
                .to("direct:elasticsearch-bulk-delete")
            .when(simple(format("${header.CamelDebeziumOperation} == '%s'", Envelope.Operation.CREATE.code())))
                .to("direct:elasticsearch-bulk-insert")
            .when(simple(format("${header.CamelDebeziumOperation} == '%s'", Envelope.Operation.UPDATE.code())))
                .to("direct:elasticsearch-bulk-update")
            .when(simple(format("${header.CamelDebeziumOperation} == '%s'", Envelope.Operation.READ.code())))
                .endChoice()
            .otherwise()
                .log(WARN, "ignore operation: ${header.CamelDebeziumOperation}");

        buildElasticSearchBulkInsertRoute();
        buildElasticSearchBulkUpdateRoute();
        buildElasticSearchBulkDeleteRoute();
        buildElasticSearchBulkTruncateRoute();
        buildElasticSearchBulkFailedRoute();
    }

    private void buildElasticSearchBulkInsertRoute() {
        from("direct:elasticsearch-bulk-insert")
            .routeId("direct:elasticsearch-bulk-insert")
            .threads(1, 10)
            .aggregate(constant(true), bulkIndexRequestOutBoxAggregate)
            .completionInterval(2_000)
            .completionSize(100)
            .to("elasticsearch-rest://docker-cluster?operation=Bulk");
    }

    private void buildElasticSearchBulkUpdateRoute() {
        from("direct:elasticsearch-bulk-update")
            .routeId("direct:elasticsearch-bulk-update")
            .threads(1, 10)
            .aggregate(constant(true), bulkIndexRequestOutBoxAggregate)
            .completionInterval(5_000)
            .completionSize(100)
            .to("elasticsearch-rest://docker-cluster?operation=Bulk");
    }

    private void buildElasticSearchBulkDeleteRoute() {
        from("direct:elasticsearch-bulk-delete")
            .routeId("direct:elasticsearch-bulk-delete")
            .log(INFO, "remove row id: ${header.CamelDebeziumKey}")
            .threads(1, 2)
            .aggregate(constant(true), bulkDeleteRequestOutBoxAggregate)
            .completionInterval(2_000)
            .completionSize(20)
            .to("elasticsearch-rest://docker-cluster?operation=Bulk");
    }

    private void buildElasticSearchBulkTruncateRoute() {
        from("direct:elasticsearch-truncate")
            .routeId("direct:elasticsearch-truncate")
            .log(INFO, "truncate table: ${header.CamelDebeziumKey}");
    }

    private void buildElasticSearchBulkFailedRoute() {
        from("direct:elasticsearch-failed")
            .routeId("direct:exception")
            .process(elasticsearchFailedProcessor)
            .to("elasticsearch-rest://docker-cluster?operation=Index");
    }
}
