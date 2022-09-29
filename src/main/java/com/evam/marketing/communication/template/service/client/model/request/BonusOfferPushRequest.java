package com.evam.marketing.communication.template.service.client.model.request;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BonusOfferPushRequest {
    public String status;
    public String msisdn;
    public String notificationTitle;
    public String notificationText;
    public String commercialText;
    public String offerPayload;
}
