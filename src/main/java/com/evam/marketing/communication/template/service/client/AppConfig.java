package com.evam.marketing.communication.template.service.client;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class AppConfig {
    @Value("${push.notifyUrl}")         public String notifyUrl;
    @Value("${push.notifyLinkUrl}")     public String notifyLinkUrl;
    @Value("${push.notifyOfferUrl}")    public String notifyOfferUrl;
    @Value("${push.notifyBonusUrl}")    public String notifyBonusUrl;
    @Value("${push.silentModeStart}")   public String silentModeStart;
    @Value("${push.silentModeEnd}")     public String silentModeEnd;
}
