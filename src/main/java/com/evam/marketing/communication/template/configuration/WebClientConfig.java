package com.evam.marketing.communication.template.configuration;

import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.RequestEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;


import java.time.Instant;
import java.util.stream.Stream;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final AppConfig appConfig;
    @Bean
    public HttpPost httpClient(){
        HttpPost httpPost = new HttpPost(appConfig.getAPI_URL());
        httpPost.setHeader("Accept", appConfig.getACCEPT_TYPE());
        httpPost.setHeader("Content-Type", appConfig.getCONTENT_TYPE());
        httpPost.setHeader("Cache-Control", appConfig.getCACHE_CONTROL());
        httpPost.setHeader("Ocp-Apim-Subscription-Key", appConfig.getOCP_APIM_SUBSCRIPTION_KEY());

        return httpPost;
    }

}
