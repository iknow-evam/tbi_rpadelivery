package com.evam.marketing.communication.template.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "push-config")
@Data
public class AppConfig {
        private String API_URL;
        private String API_VERSION;
        private String CONTENT_TYPE;
        private String ACCEPT_TYPE;
        private String OCP_APIM_SUBSCRIPTION_KEY;
        private String OCP_APIM_TRACE;
        //private Integer TIMEOUT_MILLIS;
        //private String SILENT_MODE_START;
        //private String SILENT_MODE_END;

}
