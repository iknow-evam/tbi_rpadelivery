package com.evam.marketing.communication.template.service.client.model.request.concretes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BaseNotificationRequest {

    private String key;
    private String keyType;
    private String notificationType;
    private NotificationRequest notification;
     /*
    --data '{"key": "0db4d3bb-1ace-4854-8362-f7fdd779142a",
            "keyType":"userId",
            "notificationType": "system",
            "notification": {"body": "Please push at the loan to sign it","title": "Ново известие от TBI Bank",
            "data": {"type": "link","link":"https://cashonline.tbibank.bg/","badgeCount": "6"}}}'

     */


}
