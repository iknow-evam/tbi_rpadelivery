package com.evam.marketing.communication.template.controller.CustomController.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CardDeliveryRequest {
    private String actorId;
    private String campaignName;
    private String key;
    private String value;
}
