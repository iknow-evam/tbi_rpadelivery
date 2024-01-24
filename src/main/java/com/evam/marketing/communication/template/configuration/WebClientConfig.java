package com.evam.marketing.communication.template.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final AppConfig appConfig;

    private final SearchCustomerAppConfig searchCustomerAppConfig;
    @Bean
    public WebClient webClient() {
        ConnectionProvider connectionProvider = ConnectionProvider.builder("PUSH_CONNECTION_POOL")
                .maxConnections(1000)
                .pendingAcquireMaxCount(1000)
                .build();
        ReactorClientHttpConnector clientHttpConnector = new ReactorClientHttpConnector(
                HttpClient.create(connectionProvider));
        return WebClient.builder()
                .clientConnector(clientHttpConnector)
                .baseUrl(appConfig.getAPI_URL())
                .defaultHeader("Accept", appConfig.getACCEPT_TYPE())
                .defaultHeader("Content-Type", appConfig.getCONTENT_TYPE())
                .defaultHeader("Api-version", appConfig.getAPI_VERSION())
                .defaultHeader("Ocp-Apim-Trace", appConfig.getOCP_APIM_TRACE())
                .defaultHeader("Ocp-Apim-Subscription-Key", appConfig.getOCP_APIM_SUBSCRIPTION_KEY())
                .build();
    }


    @Bean
    public WebClient searchCustomerWebClient() {
        ConnectionProvider connectionProvider = ConnectionProvider.builder("SEARCH_CUST_CONNECTION_POOL")
                .maxConnections(1000)
                .pendingAcquireMaxCount(1000)
                .build();
        ReactorClientHttpConnector clientHttpConnector = new ReactorClientHttpConnector(
                HttpClient.create(connectionProvider));
        return WebClient.builder()
                .clientConnector(clientHttpConnector)
                .baseUrl(searchCustomerAppConfig.getAPI_URL())
                .defaultHeader("Ocp-Apim-Trace", searchCustomerAppConfig.getOCP_APIM_TRACE())
                .defaultHeader("Ocp-Apim-Subscription-Key", searchCustomerAppConfig.getOCP_APIM_SUBSCRIPTION_KEY())
                .defaultHeader("Accept", appConfig.getACCEPT_TYPE())
                .build();
    }

}
