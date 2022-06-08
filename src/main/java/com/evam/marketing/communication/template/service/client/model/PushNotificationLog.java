package com.evam.marketing.communication.template.service.client.model;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;

@Data
@Entity
@Table(name = "LOG_PUSH_NOTIFICATION")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true, chain = true)
public class PushNotificationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @CreationTimestamp
    private LocalDateTime insertTime;

    private String campaignName;
    private String offerId;
    private String silentMode;
    private String msisdn;
    private String messageText;
    private String notificationType;
    private String status;
    private String fcmid;
    private String notificationTitle;
    private String notificationText;
    private String urlIdentifier;
    private String deepUrl;
    private String uaciInteractionPointName;
    private String uaciInteractiveChannelName;
    private String sessionId;
    private String commercialText;
    private String offerPayload;
    private String logger = "push";
    private String endpoint;
    private String response;
}
