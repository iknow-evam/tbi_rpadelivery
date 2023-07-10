package com.evam.marketing.communication.template.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "search-customer-config")
@Data
public class SearchCustomerAppConfig {

    private String API_URL;
    private String OCP_APIM_TRACE;
    private String OCP_APIM_SUBSCRIPTION_KEY;
}
