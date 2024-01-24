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
}
