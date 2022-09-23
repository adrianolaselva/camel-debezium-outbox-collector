package com.colector.outbox.config;

import org.apache.camel.CamelContext;
import org.apache.camel.component.elasticsearch.ElasticsearchComponent;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfig {

    @Value("${camel.component.elasticsearch-rest.host}")
    private String elasticsearchHost;

    @Value("${camel.component.elasticsearch-rest.scheme}")
    private String elasticsearchScheme;

    @Value("${camel.component.elasticsearch-rest.port}")
    private Integer elasticsearchPort;

    @Value("${camel.component.elasticsearch-rest.user}")
    private String elasticsearchUser;

    @Value("${camel.component.elasticsearch-rest.password}")
    private String elasticsearchPassword;

    @Bean
    public RestHighLevelClient elasticsearchClient() {
        return new RestHighLevelClient(restClientBuilder());
    }

    @Bean
    public RestClient restClient() {
        return restClientBuilder().build();
    }

    @Bean
    public ElasticsearchComponent elasticsearchComponent(final RestHighLevelClient elasticsearchClient) {
        final var elasticsearchComponent = new ElasticsearchComponent();
        elasticsearchComponent.setClient(elasticsearchClient.getLowLevelClient());
        return elasticsearchComponent;
    }

    public RestClientBuilder restClientBuilder() {
        Header[] headers = {
            new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json"),
            new BasicHeader("Role", "Read")
        };

        final CredentialsProvider basicCredentialsProvider = new BasicCredentialsProvider();

        RestClientBuilder restClientBuilder = RestClient
            .builder(new HttpHost(elasticsearchHost, elasticsearchPort, elasticsearchScheme))
            .setDefaultHeaders(headers);

        if (!elasticsearchUser.isEmpty()) {
            basicCredentialsProvider
                .setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(elasticsearchUser, elasticsearchPassword));

            restClientBuilder
                .setHttpClientConfigCallback(arg0 ->
                    arg0.setDefaultCredentialsProvider(basicCredentialsProvider));

        }

        return restClientBuilder;
    }

}
