package com.evam.marketing.communication.template.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.LocalTime;

@Configuration
@ConfigurationProperties(prefix = "rpa-speedy-config")
@Data
public class AppConfig {
        private String API_URL;
        private String OCP_APIM_SUBSCRIPTION_KEY;
        private String CACHE_CONTROL;
        private String CONTENT_TYPE;
        private String ACCEPT_TYPE;
        private String TIMEOUT_MILLIS;

        private int CHUNK_SIZE;


        private String DB_URL;
        private String DB_USERNAME;
        private String DB_PASSWORD;
        private String DB_TABLE_NAME;
        private String HOST;
        private String DATABASE;


}
