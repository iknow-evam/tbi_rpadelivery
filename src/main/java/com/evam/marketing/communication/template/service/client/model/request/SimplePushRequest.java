package com.evam.marketing.communication.template.service.client.model.request;

import lombok.*;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimplePushRequest implements Serializable {
    public String msisdn;
    public String notificationType;
    public String status;
    public String fcmId;
}
